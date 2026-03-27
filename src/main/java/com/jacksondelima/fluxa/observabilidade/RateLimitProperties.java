package com.jacksondelima.fluxa.observabilidade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "fluxa.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    @Valid
    private Policy auth = new Policy(10, 60);

    @Valid
    private Policy admin = new Policy(60, 60);

    @Valid
    private Policy api = new Policy(120, 60);

    @Getter
    @Setter
    public static class Policy {

        @Min(1)
        private int maxRequests;

        @Min(1)
        private int windowSeconds;

        public Policy() {
        }

        public Policy(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
}
