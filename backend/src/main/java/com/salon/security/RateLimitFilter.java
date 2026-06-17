package com.salon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 登录限流过滤器
 * <p>
 * 基于 IP 的简易令牌桶限流，限制 POST /api/auth/login 的频率。
 * 默认：每 IP 每分钟 5 次，超过返回 HTTP 429。
 * </p>
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    /** 登录接口：每 IP 每分钟最大 5 次 */
    private static final int LOGIN_MAX_PER_MINUTE = 5;

    /** 统计窗口（毫秒），1 分钟 */
    private static final long WINDOW_MS = 60_000L;

    /** 是否信任代理头（生产环境在可信反向代理后启用） */
    @Value("${server.trust-proxy:false}")
    private boolean trustProxy;

    /** 路径+IP → 请求记录 */
    // TODO(N30): 替换为 Redis 存储，避免重启丢失 + 多实例不同步
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(
            () -> counters.entrySet().removeIf(e ->
                System.currentTimeMillis() - e.getValue().windowStart > WINDOW_MS),
            5, 5, TimeUnit.MINUTES);
    }

    /**
     * 滑动窗口计数器（每个实例有独立的 maxRequests）
     */
    private static class WindowCounter {
        final int maxRequests;
        volatile long windowStart;
        volatile int count;

        WindowCounter(long start, int maxRequests) {
            this.windowStart = start;
            this.count = 1;
            this.maxRequests = maxRequests;
        }

        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                windowStart = now;
                count = 1;
                return true;
            }
            if (count < maxRequests) {
                count++;
                return true;
            }
            return false;
        }

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitRule rule = matchRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String key = rule.path + "|" + ip;
        WindowCounter counter = counters.computeIfAbsent(key,
                k -> new WindowCounter(System.currentTimeMillis(), rule.maxPerMinute));

        if (!counter.tryAcquire()) {
            log.warn("IP {} 对 {} 频率超限，已拒绝", ip, rule.path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"" + rule.rejectMsg + "\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 匹配需要限流的请求，返回限流规则；不匹配则返回 null
     */
    private RateLimitRule matchRule(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String uri = request.getRequestURI();
        if ("/api/auth/login".equals(uri)) {
            return new RateLimitRule("/api/auth/login", LOGIN_MAX_PER_MINUTE,
                    "操作过于频繁，请稍后再试（每分钟最多5次）");
        }
        // 发送验证码限流已移至 CustomerAuthServiceImpl.sendCode() 内，
        // 仅对成功发送计次，手机号格式错误/未注册等不消耗配额
        return null;
    }

    /**
     * 限流规则
     */
    private record RateLimitRule(String path, int maxPerMinute, String rejectMsg) {}

    /**
     * 获取客户端真实 IP
     * <p>
     * 仅在 {@code server.trust-proxy=true}（可信反向代理后）时使用 X-Forwarded-For / X-Real-IP 头；
     * 否则直接使用 {@code request.getRemoteAddr()}，防止 IP 伪造。
     * </p>
     */
    private String getClientIp(HttpServletRequest request) {
        if (trustProxy) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
