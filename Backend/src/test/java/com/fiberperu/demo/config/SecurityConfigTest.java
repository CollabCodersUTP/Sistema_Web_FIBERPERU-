package com.fiberperu.demo.config;

import com.fiberperu.demo.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig(
            mock(JwtAuthenticationFilter.class));

    @Test
    void permiteSoloLosOrigenesConfigurados() {
        var source = config.corsConfigurationSource(
                " https://portal.example.test, http://localhost:3000 ");
        var cors = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/v1/ordenes"));

        assertEquals(List.of("https://portal.example.test", "http://localhost:3000"),
                cors.getAllowedOrigins());
        assertNull(cors.getAllowedOriginPatterns());
    }

    @Test
    void rechazaComodinesYListaVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                config.corsConfigurationSource("https://*.azurewebsites.net"));
        assertThrows(IllegalArgumentException.class, () ->
                config.corsConfigurationSource(" , "));
    }
}
