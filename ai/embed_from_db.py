"""
数据管道：MySQL 真实会员 → Embedding → ChromaDB
替代 embed_members.py（模拟数据），连接真实数据库生成向量
"""

import os, sys
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
os.environ["PYTHONIOENCODING"] = "utf-8"

import pymysql
from chromadb import PersistentClient, Settings
from chromadb.utils import embedding_functions

# ── 配置 ──
DB_DIR = os.path.join(os.path.dirname(__file__), "..", "chroma_db")
COLLECTION_NAME = "member_descriptions"

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASSWORD", ""),
    "database": os.getenv("DB_NAME", "salon"),
    "charset": "utf8mb4"
}

MODEL_NAME = "BAAI/bge-small-zh-v1.5"

# ── Step 1: 加载 BGE 向量模型 ──
print("[1/4] 加载 BGE 模型...")
embedding_fn = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name=MODEL_NAME, device="cpu"
)
print("      ✓ 模型就绪\n")

# ── Step 2: 从 MySQL 读取真实会员 + 拼接描述文本 ──
#
# 【AI 知识 — 描述文本的重要性】
# 向量搜索的质量 80% 取决于"描述文本写得好不好"。
# 模型通过这段文字理解会员特征：
#   "高消费" → 向量靠近"高价值客户"
#   "金卡会员，最近来过" → 向量靠近"活跃会员"
#   "余额充足，消费频繁" → 向量靠近"优质客户"
#
# 描述文本 = 把数据库的结构化字段翻译成自然语言
# 字段越多、越具体，搜索效果越好
#
print("[2/4] 从 MySQL 读取会员数据...")

conn = pymysql.connect(**DB_CONFIG)
cursor = conn.cursor()
cursor.execute("""
    SELECT
        m.id, m.name, m.phone,
        COALESCE(ml.name, '普通会员') AS level_name,
        m.points, m.balance,
        COALESCE(m.tags, '') AS tags,
        CASE m.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '未知' END AS gender_text,
        COALESCE(m.remark, '') AS remark,
        COALESCE(m.last_consume_date, '无记录') AS last_consume,
        COALESCE((SELECT SUM(total_amount) FROM consumption_order WHERE member_id = m.id), 0) AS total_spent,
        (SELECT COUNT(*) FROM consumption_order WHERE member_id = m.id) AS order_count
    FROM member m
    LEFT JOIN member_level ml ON m.level = ml.id
    ORDER BY m.id
""")

docs, ids, metadatas = [], [], []
for row in cursor:
    (mid, name, phone, level, points, balance,
     tags, gender, remark, last_consume, total_spent, order_count) = row

    # 拼接自然语言描述（这是送给 AI 模型的"会员画像"）
    parts = [f"会员{name}"]
    parts.append(f"{gender}")
    if level:
        parts.append(f"{level}")
    parts.append(f"积分{points}，余额{balance:.0f}元")
    if total_spent > 0:
        parts.append(f"累计消费{total_spent:.0f}元共{order_count}次")
    parts.append(f"最近消费{last_consume}")
    if tags:
        parts.append(f"标签：{tags}")
    if remark:
        parts.append(f"备注：{remark}")

    description = "，".join(parts)

    docs.append(description)
    ids.append(str(mid))           # ID 用真实 member.id，搜索结果可直接跳转
    # 手机号脱敏后存储：138****1234
    masked_phone = (phone[:3] + "****" + phone[-4:]) if phone and len(phone) >= 7 else (phone or "")
    metadatas.append({
        "member_id": mid,
        "display_name": name,
        "phone": masked_phone,
        "level": level,
    })

cursor.close()
conn.close()
print(f"      ✓ 读取 {len(docs)} 条会员\n")
for i, d in enumerate(docs):
    print(f"      {i+1}. {d}")

# ── Step 3: 存入 ChromaDB ──
print(f"\n[3/4] 生成 Embedding 并写入 ChromaDB...")

client = PersistentClient(path=DB_DIR, settings=Settings(anonymized_telemetry=False))

# 幂等重建
try:
    client.delete_collection(COLLECTION_NAME)
except Exception:
    pass

collection = client.create_collection(
    name=COLLECTION_NAME,
    embedding_function=embedding_fn,
    metadata={"hnsw:space": "cosine"}
)

# Chroma 自动调用 embedding_fn 把每段描述转成 512 维向量
collection.add(documents=docs, ids=ids, metadatas=metadatas)
print(f"      ✓ 已存入 {collection.count()} 条向量\n")

# ── Step 4: 自检 — 搜一次验证 ──
print("[4/4] 自检搜索...")
results = collection.query(query_texts=["高消费活跃会员"], n_results=3,
                           include=["documents", "distances", "metadatas"])

for i, (doc, dist, meta) in enumerate(zip(
    results["documents"][0], results["distances"][0], results["metadatas"][0]
)):
    sim = (1 - dist) * 100
    print(f"      #{i+1} [{meta['display_name']}] 相似度 {sim:.1f}%  |  {doc[:80]}...")

print(f"\n{'='*60}")
print("数据管道完成！真实会员已接入 AI 搜索。")
print(f"向量库路径: {os.path.abspath(DB_DIR)}")
print(f"下次搜索 '高消费会员' 或 'zj' 即可命中真实数据")
print(f"{'='*60}")
