package com.personalfinance.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "rawAllowedOrigins", "http://localhost:4200");
    }

    @Test
    void corsWebFilterBeanIsCreated() {
        CorsWebFilter filter = corsConfig.corsWebFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    void corsWebFilterIsCreatedWithMultipleAllowedOrigins() {
        ReflectionTestUtils.setField(corsConfig, "rawAllowedOrigins", "http://localhost:4200,https://app.personalfinance.com");

        CorsWebFilter filter = corsConfig.corsWebFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    void corsWebFilterIsCreatedWithSingleProductionOrigin() {
        ReflectionTestUtils.setField(corsConfig, "rawAllowedOrigins", "https://app.personalfinance.com");

        CorsWebFilter filter = corsConfig.corsWebFilter();

        assertThat(filter).isNotNull();
    }
}

