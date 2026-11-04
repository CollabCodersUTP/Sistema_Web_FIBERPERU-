package com.fiberperu.demo.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class HistorialOrdenTest {

    @Test
    void registraHoraDeLimaAunqueElServidorUseUtc() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            HistorialOrden historial = new HistorialOrden();
            historial.antesDeCrear();

            LocalDateTime horaLima = LocalDateTime.now(ZoneId.of("America/Lima"));
            assertTrue(Duration.between(historial.getFechaHora(), horaLima).abs().toSeconds() < 2);
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
