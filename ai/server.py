"""
FastAPI 语义搜索微服务
启动: uvicorn ai.server:app --port 8000
"""

import os
import sys

os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

from chromadb import PersistentClient, Settings
from chromadb.utils import embedding_functions
from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(title="Salon AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ORIGINS", "http://localhost:3000").split(","),
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

DB_DIR = os.path.join(os.path.dirname(__file__), "..", "chroma_db")
COLLECTION_NAME = "member_descriptions"

embedding_fn = None
collection = None


def init_chroma():
    global embedding_fn, collection
    if not os.path.exists(DB_DIR):
        raise FileNotFoundError(f"chroma_db not found at {DB_DIR}. Run ai/embed_members.py first.")
    embedding_fn = embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name="BAAI/bge-small-zh-v1.5", device="cpu"
    )
    client = PersistentClient(path=DB_DIR, settings=Settings(anonymized_telemetry=False))
    collection = client.get_collection(name=COLLECTION_NAME, embedding_function=embedding_fn)
    print(f"[AI Service] ChromaDB ready, {collection.count()} vectors loaded")


class SearchResult(BaseModel):
    member_id: int
    name: str
    description: str
    similarity: float


class SearchResponse(BaseModel):
    query: str
    results: list[SearchResult]


@app.on_event("startup")
def startup():
    init_chroma()


@app.get("/api/ai/health")
def health():
    return {"status": "ok", "vectors": collection.count() if collection else 0}


@app.get("/api/ai/search")
def search(q: str = Query(..., description="Natural language search query"), top_k: int = Query(5, ge=1, le=20)):
    results = collection.query(query_texts=[q], n_results=top_k, include=["documents", "distances", "metadatas"])
    items = []
    for doc, dist, meta in zip(results["documents"][0], results["distances"][0], results["metadatas"][0]):
        items.append(SearchResult(
            member_id=meta.get("member_id", 0),
            name=meta["display_name"],
            description=doc,
            similarity=round(max(0, (1 - dist) * 100), 1)
        ))
    return SearchResponse(query=q, results=items).model_dump()


if __name__ == "__main__":
    import uvicorn
    host = os.getenv("AI_HOST", "127.0.0.1")
    uvicorn.run(app, host=host, port=8000)
