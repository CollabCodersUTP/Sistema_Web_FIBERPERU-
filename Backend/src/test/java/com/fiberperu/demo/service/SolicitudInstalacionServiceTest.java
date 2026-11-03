package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.solicitud.EvaluarSolicitudRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionResponse;
import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.HistorialSolicitud;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.HistorialSolicitudRepository;
import com.fiberperu.demo.repository.SolicitudInstalacionRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.verify;

class SolicitudInstalacionServiceTest {

    private final SolicitudInstalacionRepository solicitudRepository =
            mock(SolicitudInstalacionRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final CodigoSolicitudService codigoSolicitudService = mock(CodigoSolicitudService.class);
    private final HistorialSolicitudRepository historialRepository = mock(HistorialSolicitudRepository.class);
    private final SolicitudInstalacionService service =
            new SolicitudInstalacionService(
                    solicitudRepository,
                    clienteRepository,
                    usuarioRepository,
                    codigoSolicitudService,
                    historialRepository
            );

    @Test
    void registraFechaEnLaPrimeraEvaluacion() {
        SolicitudInstalacion solicitud = solicitud("REGISTRADA", null);
        LocalDateTime inicio = LocalDateTime.now(ZoneId.of("America/Lima"));
        prepararRepositorio(solicitud);

        SolicitudInstalacionResponse respuesta = service.evaluar(
                1L, EvaluarSolicitudRequest.builder()
                        .resultado("APROBADA")
                        .build(), "admin@fiberperu.test"
        );

        assertEquals("APROBADA", respuesta.getEstado());
        assertNotNull(respuesta.getFechaEvaluacion());
        assertTrue(!respuesta.getFechaEvaluacion().isBefore(inicio));
        ArgumentCaptor<HistorialSolicitud> evento = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("EVALUACION", evento.getValue().getTipoEvento());
        assertEquals("REGISTRADA", evento.getValue().getEstadoAnterior());
        assertEquals("APROBADA", evento.getValue().getEstadoNuevo());
        assertEquals(2L, evento.getValue().getUsuario().getIdUsuario());
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
                        .build(), "admin@fiberperu.test"
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
                        .build(), "admin@fiberperu.test"
        );

        assertNull(respuesta.getFechaEvaluacion());
    }

    @Test
    void registraAutorYEstadoInicialDeLaSolicitud() {
        Usuario responsable = Usuario.builder()
                .idUsuario(2L).correo("admin@fiberperu.test")
                .nombres("Carlos").apellidos("Mendoza").build();
        Cliente cliente = solicitud("REGISTRADA", null).getCliente();
        when(usuarioRepository.findByCorreo(responsable.getCorreo()))
                .thenReturn(Optional.of(responsable));
        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));
        when(codigoSolicitudService.generar()).thenReturn("SOL-2026-ABC123-1001");
        when(solicitudRepository.save(any(SolicitudInstalacion.class)))
                .thenAnswer(invocacion -> {
                    SolicitudInstalacion guardada = invocacion.getArgument(0);
                    guardada.setIdSolicitud(1L);
                    return guardada;
                });

        service.registrar(SolicitudInstalacionRequest.builder()
                .idCliente(3L)
                .tipoServicio("Internet")
                .descripcionServicio("Instalación")
                .direccionInstalacion("Lima")
                .build(), responsable.getCorreo(), false);

        ArgumentCaptor<HistorialSolicitud> evento = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("CREACION", evento.getValue().getTipoEvento());
        assertEquals("REGISTRADA", evento.getValue().getEstadoNuevo());
        assertEquals(responsable, evento.getValue().getUsuario());
        assertEquals(1L, evento.getValue().getSolicitud().getIdSolicitud());
    }

    private void prepararRepositorio(SolicitudInstalacion solicitud) {
        when(usuarioRepository.findByCorreo("admin@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder()
                        .idUsuario(2L)
                        .nombres("Carlos")
                        .apellidos("Mendoza")
                        .correo("admin@fiberperu.test")
                        .build()));
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
