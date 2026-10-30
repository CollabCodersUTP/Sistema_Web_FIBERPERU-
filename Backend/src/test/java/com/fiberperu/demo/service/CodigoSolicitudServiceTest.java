package com.fiberperu.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodigoSolicitudServiceTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SecureRandom aleatorio = mock(SecureRandom.class);

    @Test
    void generaCodigoConAnioDeLimaYConsecutivoDeBaseDeDatos() {
        Clock reloj = Clock.fixed(Instant.parse("2027-01-01T02:00:00Z"), ZoneId.of("America/Lima"));
        when(aleatorio.nextInt(anyInt())).thenReturn(0);
        when(jdbcTemplate.update(contains("solicitud_codigo_bloque"), anyString())).thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("solicitud_codigo_consecutivo"), eq(Integer.class), eq(2026)))
                .thenReturn(1001);

        String codigo = new CodigoSolicitudService(jdbcTemplate, reloj, aleatorio).generar();

        assertEquals("SOL-2026-AAAAAA-1001", codigo);
        assertEquals(20, codigo.length());
        verify(jdbcTemplate).update(contains("solicitud_codigo_bloque"), eq("AAAAAA"));
    }

    @Test
    void cambiaDeAnioYSigueConUnBloqueNuevoSiElPrimeroYaExiste() {
        Clock reloj = Clock.fixed(Instant.parse("2027-01-01T05:00:00Z"), ZoneId.of("America/Lima"));
        when(aleatorio.nextInt(anyInt())).thenReturn(0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1);
        when(jdbcTemplate.update(contains("solicitud_codigo_bloque"), anyString()))
                .thenReturn(0, 1);
        when(jdbcTemplate.queryForObject(contains("solicitud_codigo_consecutivo"), eq(Integer.class), eq(2027)))
                .thenReturn(1);

        assertEquals("SOL-2027-BBBBBB-0001",
                new CodigoSolicitudService(jdbcTemplate, reloj, aleatorio).generar());
    }

    @Test
    void rechazaAlAgotarseElConsecutivoAnual() {
        Clock reloj = Clock.fixed(Instant.parse("2027-01-01T05:00:00Z"), ZoneId.of("America/Lima"));
        when(aleatorio.nextInt(anyInt())).thenReturn(0);
        when(jdbcTemplate.update(contains("solicitud_codigo_bloque"), anyString())).thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("solicitud_codigo_consecutivo"), eq(Integer.class), eq(2027)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(IllegalStateException.class,
                () -> new CodigoSolicitudService(jdbcTemplate, reloj, aleatorio).generar());
    }
}
