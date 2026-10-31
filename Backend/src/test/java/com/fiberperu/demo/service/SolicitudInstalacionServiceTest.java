package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.solicitud.EvaluarSolicitudRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionResponse;
import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.SolicitudInstalacionRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SolicitudInstalacionServiceTest {

    private final SolicitudInstalacionRepository solicitudRepository =
            mock(SolicitudInstalacionRepository.class);
    private final SolicitudInstalacionService service =
            new SolicitudInstalacionService(
                    solicitudRepository,
                    mock(ClienteRepository.class),
                    mock(UsuarioRepository.class),
                    mock(CodigoSolicitudService.class)
            );

    @Test
    void registraFechaEnLaPrimeraEvaluacion() {
        SolicitudInstalacion solicitud = solicitud("REGISTRADA", null);
        LocalDateTime inicio = LocalDateTime.now(ZoneId.of("America/Lima"));
        prepararRepositorio(solicitud);

        SolicitudInstalacionResponse respuesta = service.evaluar(
                1L, EvaluarSolicitudRequest.builder()
                        .resultado("APROBADA")
                        .build()
        );

        assertEquals("APROBADA", respuesta.getEstado());
        assertNotNull(respuesta.getFechaEvaluacion());
        assertTrue(!respuesta.getFechaEvaluacion().isBefore(inicio));
    }

    @Test
    void conservaLaFechaOriginalAlReevaluar() {
        SolicitudInstalacion solicitud = solicitud("APROBADA", "APROBADA");
        LocalDateTime primeraEvaluacion = LocalDateTime.of(2026, 10, 30, 10, 15);
        solicitud.setFechaEvaluacion(primeraEvaluacion);
        prepararRepositorio(solicitud);

        SolicitudInstalacionResponse respuesta = service.evaluar(
                1L, EvaluarSolicitudRequest.builder()
                        .resultado("RECHAZADA")
                        .build()
        );

        assertEquals(primeraEvaluacion, respuesta.getFechaEvaluacion());
    }

    @Test
    void noInventaFechaHistoricaEnUnaSolicitudYaEvaluada() {
        SolicitudInstalacion solicitud = solicitud("APROBADA", "APROBADA");
        prepararRepositorio(solicitud);

        SolicitudInstalacionResponse respuesta = service.evaluar(
                1L, EvaluarSolicitudRequest.builder()
                        .resultado("RECHAZADA")
                        .build()
        );

        assertNull(respuesta.getFechaEvaluacion());
    }

    private void prepararRepositorio(SolicitudInstalacion solicitud) {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudInstalacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private SolicitudInstalacion solicitud(String estado, String resultado) {
        Usuario usuario = Usuario.builder()
                .nombres("Ana")
                .apellidos("Torres")
                .build();
        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .numeroDocumento("12345678")
                .build();
        return SolicitudInstalacion.builder()
                .idSolicitud(1L)
                .cliente(cliente)
                .codigoSolicitud("SOL-2026-ABC123-1001")
                .fechaSolicitud(LocalDateTime.of(2026, 10, 30, 9, 0))
                .estado(estado)
                .resultadoEvaluacion(resultado)
                .build();
    }
}
