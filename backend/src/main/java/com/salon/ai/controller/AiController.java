package com.salon.ai.controller;

import com.salon.ai.dto.AiSearchResponse;
import com.salon.common.exception.ErrorCode;
import com.salon.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Tag(name = "AI 智能搜索", description = "语义搜索会员接口")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final RestClient restClient;

    public AiController(@Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().baseUrl(aiServiceUrl).requestFactory(factory).build();
    }

    @GetMapping("/health")
    @Operation(summary = "AI服务健康检查")
    public Result<?> health() {
        try {
            var resp = restClient.get().uri("/api/ai/health").retrieve().body(java.util.Map.class);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI 服务不可用: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "语义搜索会员", description = "用自然语言搜索最匹配的会员")
    public Result<AiSearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            var resp = restClient.get()
                    .uri(uri -> uri.path("/api/ai/search").queryParam("q", q).queryParam("top_k", topK).build())
                    .retrieve()
                    .body(AiSearchResponse.class);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error(ErrorCode.AI_SEARCH_FAILED, "AI 搜索失败: " + e.getMessage());
        }
    }
}
