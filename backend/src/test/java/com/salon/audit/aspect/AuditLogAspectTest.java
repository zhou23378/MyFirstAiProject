package com.salon.audit.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salon.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogAspectTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditLogAspect aspect = new AuditLogAspect(null, objectMapper);

    @Test
    void maskSensitiveParams_shouldMaskSensitiveFieldsRecursively() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("phone", "13800138000");
        nested.put("access_token", "access-token-value");
        nested.put("clientSecret", "client-secret-value");
        nested.put("smsCode", "123456");
        nested.put("auth", "bearer-secret-value");
        nested.put("settings", Map.of("apiKey", "api-key-value"));
        nested.put("items", List.of(Map.of("private_key", "private-key-value")));

        String params = aspect.maskSensitiveParams(
                new Object[] {loginRequest, nested},
                new String[] {"req", "payload"});

        JsonNode root = objectMapper.readTree(params);
        assertThat(root.get(0).get("username").asText()).isEqualTo("admin");
        assertThat(root.get(0).get("password").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("phone").asText()).isEqualTo("138****8000");
        assertThat(root.get(1).get("access_token").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("clientSecret").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("smsCode").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("auth").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("settings").get("apiKey").asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).get("items").get(0).get("private_key").asText()).isEqualTo("***MASKED***");
        assertThat(params)
                .doesNotContain("admin123")
                .doesNotContain("access-token-value")
                .doesNotContain("client-secret-value")
                .doesNotContain("123456")
                .doesNotContain("bearer-secret-value")
                .doesNotContain("api-key-value")
                .doesNotContain("private-key-value");
    }

    @Test
    void maskSensitiveParams_shouldMaskSensitiveParameterName() throws Exception {
        String params = aspect.maskSensitiveParams(
                new Object[] {"plain-token-value", "visible"},
                new String[] {"token", "keyword"});

        JsonNode root = objectMapper.readTree(params);
        assertThat(root.get(0).asText()).isEqualTo("***MASKED***");
        assertThat(root.get(1).asText()).isEqualTo("visible");
        assertThat(params).doesNotContain("plain-token-value");
    }
}
