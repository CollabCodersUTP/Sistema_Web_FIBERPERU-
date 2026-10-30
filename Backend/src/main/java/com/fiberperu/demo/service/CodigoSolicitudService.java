package com.fiberperu.demo.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class CodigoSolicitudService {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_BLOQUE = 6;
    private static final int MAX_INTENTOS = 20;

    private final JdbcTemplate jdbcTemplate;
    private final Clock reloj;
    private final SecureRandom aleatorio;

    @Autowired
    public CodigoSolicitudService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.system(ZoneId.of("America/Lima")), new SecureRandom());
    }

    CodigoSolicitudService(JdbcTemplate jdbcTemplate, Clock reloj, SecureRandom aleatorio) {
        this.jdbcTemplate = jdbcTemplate;
        this.reloj = reloj;
        this.aleatorio = aleatorio;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String generar() {
        String bloque = reservarBloque();
        int anio = LocalDate.now(reloj).getYear();
        Integer numero;
        try {
            numero = jdbcTemplate.queryForObject("""
                    INSERT INTO solicitud_codigo_consecutivo (anio, ultimo_numero)
                    VALUES (?, 1)
                    ON CONFLICT (anio) DO UPDATE
                    SET ultimo_numero = solicitud_codigo_consecutivo.ultimo_numero + 1
                    WHERE solicitud_codigo_consecutivo.ultimo_numero < 9999
                    RETURNING ultimo_numero
                    """, Integer.class, anio);
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalStateException("Se agotaron los números de solicitud para " + anio, ex);
        }
        return String.format(Locale.ROOT, "SOL-%d-%s-%04d", anio, bloque, numero);
    }

    private String reservarBloque() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            StringBuilder bloque = new StringBuilder(LONGITUD_BLOQUE);
            for (int i = 0; i < LONGITUD_BLOQUE; i++) {
                bloque.append(CARACTERES.charAt(aleatorio.nextInt(CARACTERES.length())));
            }
            String candidato = bloque.toString();
            int reservado = jdbcTemplate.update("""
                    INSERT INTO solicitud_codigo_bloque (bloque)
                    VALUES (?) ON CONFLICT (bloque) DO NOTHING
                    """, candidato);
            if (reservado == 1) {
                return candidato;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de solicitud único");
    }
}
