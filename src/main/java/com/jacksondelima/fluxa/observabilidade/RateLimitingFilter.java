package com.jacksondelima.fluxa.observabilidade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacksondelima.fluxa.excecao.ErroResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int TOO_MANY_REQUESTS = HttpStatus.TOO_MANY_REQUESTS.value();

    private final RateLimitProperties rateLimitProperties;
    private final RateLimitingService rateLimitingService;
    private final FluxaMetricsService fluxaMetricsService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(
            RateLimitProperties rateLimitProperties,
            RateLimitingService rateLimitingService,
            FluxaMetricsService fluxaMetricsService,
            ObjectMapper objectMapper
    ) {
        this.rateLimitProperties = rateLimitProperties;
        this.rateLimitingService = rateLimitingService;
        this.fluxaMetricsService = fluxaMetricsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitPolicy policy = resolvePolicy(request);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = resolveIdentifier(request, policy.bucket());
        RateLimitDecision decision = rateLimitingService.tryConsume(
                policy.bucket(),
                identifier,
                policy.maxRequests(),
                Duration.ofSeconds(policy.windowSeconds())
        );

        response.setHeader("X-Rate-Limit-Limit", String.valueOf(policy.maxRequests()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(decision.remainingRequests()));

        if (!decision.allowed()) {
            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            response.setStatus(TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            fluxaMetricsService.registrarRateLimitExcedido();
            log.warn("rate_limit_exceeded bucket={} identifier={} path={}", policy.bucket(), identifier, request.getRequestURI());

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErroResponseDTO(
                            "rate_limit",
                            "Limite de requisicoes excedido. Tente novamente em instantes.",
                            TOO_MANY_REQUESTS,
                            request.getRequestURI(),
                            Instant.now()
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/assets/")
                || "/favicon.ico".equals(path)
                || "/".equals(path)
                || "/index.html".equals(path)
                || path.startsWith("/actuator/health");
    }

    private RateLimitPolicy resolvePolicy(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/autenticacao/")) {
            return new RateLimitPolicy(
                    "auth",
                    rateLimitProperties.getAuth().getMaxRequests(),
                    rateLimitProperties.getAuth().getWindowSeconds()
            );
        }

        if (path.startsWith("/administracao/")) {
            return new RateLimitPolicy(
                    "admin",
                    rateLimitProperties.getAdmin().getMaxRequests(),
                    rateLimitProperties.getAdmin().getWindowSeconds()
            );
        }

        if (path.startsWith("/tarefas/") || path.startsWith("/usuarios/")) {
            return new RateLimitPolicy(
                    "api",
                    rateLimitProperties.getApi().getMaxRequests(),
                    rateLimitProperties.getApi().getWindowSeconds()
            );
        }

        return null;
    }

    private String resolveIdentifier(HttpServletRequest request, String bucket) {
        if ("auth".equals(bucket)) {
            return resolveClientIp(request);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName().toLowerCase(Locale.ROOT);
        }

        return resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private record RateLimitPolicy(
            String bucket,
            int maxRequests,
            int windowSeconds
    ) {
    }
}
