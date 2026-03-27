package com.jacksondelima.fluxa.configuracao;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.tracer.common.SpanContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({PrometheusMeterRegistry.class, PrometheusScrapeEndpoint.class})
public class PrometheusMetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = "management.prometheus.metrics.export.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    PrometheusConfig prometheusConfig() {
        return PrometheusConfig.DEFAULT;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = "management.prometheus.metrics.export.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    PrometheusRegistry prometheusRegistry() {
        return new PrometheusRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = "management.prometheus.metrics.export.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    PrometheusMeterRegistry prometheusMeterRegistry(
            PrometheusConfig prometheusConfig,
            PrometheusRegistry prometheusRegistry,
            Clock clock,
            ObjectProvider<SpanContext> spanContext
    ) {
        return new PrometheusMeterRegistry(
                prometheusConfig,
                prometheusRegistry,
                clock,
                spanContext.getIfAvailable()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint(PrometheusScrapeEndpoint.class)
    PrometheusScrapeEndpoint prometheusEndpoint(
            PrometheusRegistry prometheusRegistry,
            PrometheusConfig prometheusConfig
    ) {
        return new PrometheusScrapeEndpoint(
                prometheusRegistry,
                prometheusConfig.prometheusProperties()
        );
    }
}
