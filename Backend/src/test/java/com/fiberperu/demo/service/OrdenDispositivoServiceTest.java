package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.asignacion.RetirarDispositivoRequest;
import com.fiberperu.demo.dto.asignacion.OrdenDispositivoRequest;
import com.fiberperu.demo.entity.Dispositivo;
import com.fiberperu.demo.entity.HistorialOrden;
import com.fiberperu.demo.entity.OrdenDispositivo;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.TipoDispositivo;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.DispositivoRepository;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenDispositivoRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenDispositivoServiceTest {

    @Mock private OrdenDispositivoRepository ordenDispositivoRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock private DispositivoRepository dispositivoRepository;
    @Mock private HistorialOrdenRepository historialRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TecnicoRepository tecnicoRepository;
    @InjectMocks private OrdenDispositivoService service;

    @ParameterizedTest
    @ValueSource(strings = {"FINALIZADA", "CANCELADA"})
    void noMarcaDispositivoInstaladoEnOrdenCerrada(String estadoOrden) {
        OrdenDispositivo asignacion = asignacion(estadoOrden);
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.marcarComoInstalado(8L, "admin@fiberperu.test", true));

        assertEquals("No se pueden modificar dispositivos de una orden cerrada",
                error.getMessage());
        assertEquals("ASIGNADO", asignacion.getEstadoAsignacion());
        verifyNoInteractions(dispositivoRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FINALIZADA", "CANCELADA"})
    void noRetiraDispositivoDeOrdenCerrada(String estadoOrden) {
        OrdenDispositivo asignacion = asignacion(estadoOrden);
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.retirar(8L, new RetirarDispositivoRequest(),
                        "admin@fiberperu.test", true));

        assertEquals("No se pueden modificar dispositivos de una orden cerrada",
                error.getMessage());
        assertEquals("ASIGNADO", asignacion.getEstadoAsignacion());
        verifyNoInteractions(dispositivoRepository);
    }

    @Test
    void registraResponsableAlAsignarDispositivo() {
        OrdenDispositivo asignacion = asignacion("EN_PROCESO");
        Dispositivo dispositivo = asignacion.getDispositivo();
        dispositivo.setEstado("EN_STOCK");
        dispositivo.setDisponibilidad(true);
        when(ordenTrabajoRepository.findById(7L))
                .thenReturn(Optional.of(asignacion.getOrden()));
        when(dispositivoRepository.findById(4L))
                .thenReturn(Optional.of(dispositivo));
        when(ordenDispositivoRepository.findByDispositivoIdDispositivoAndFechaRetiroIsNull(4L))
                .thenReturn(Optional.empty());
        when(ordenDispositivoRepository.save(org.mockito.ArgumentMatchers.any(OrdenDispositivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Usuario actor = Usuario.builder().idUsuario(1L).build();
        when(usuarioRepository.findByCorreo("admin@fiberperu.test"))
                .thenReturn(Optional.of(actor));

        service.asignar(OrdenDispositivoRequest.builder()
                .idOrden(7L).idDispositivo(4L).build(),
                "admin@fiberperu.test", true);

        ArgumentCaptor<HistorialOrden> evento = ArgumentCaptor.forClass(HistorialOrden.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("ASIGNACION_DISPOSITIVO", evento.getValue().getTipoEvento());
        assertEquals(actor, evento.getValue().getUsuario());
    }

    @Test
    void permiteMarcarInstaladoEnOrdenAbierta() {
        OrdenDispositivo asignacion = asignacion("EN_PROCESO");
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));
        when(ordenDispositivoRepository.save(asignacion)).thenReturn(asignacion);
        when(usuarioRepository.findByCorreo("admin@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(1L).build()));

        var respuesta = service.marcarComoInstalado(8L, "admin@fiberperu.test", true);

        assertEquals("INSTALADO", respuesta.getEstadoAsignacion());
        assertEquals("INSTALADO", asignacion.getDispositivo().getEstado());
        ArgumentCaptor<HistorialOrden> evento = ArgumentCaptor.forClass(HistorialOrden.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("INSTALACION_DISPOSITIVO", evento.getValue().getTipoEvento());
    }

    @Test
    void permiteRetirarDispositivoDeOrdenAbierta() {
        OrdenDispositivo asignacion = asignacion("EN_PROCESO");
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));
        when(ordenDispositivoRepository.save(asignacion)).thenReturn(asignacion);
        when(usuarioRepository.findByCorreo("admin@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(1L).build()));

        var respuesta = service.retirar(8L, new RetirarDispositivoRequest(),
                "admin@fiberperu.test", true);

        assertEquals("RETIRADO", respuesta.getEstadoAsignacion());
        assertEquals("EN_STOCK", asignacion.getDispositivo().getEstado());
        assertTrue(asignacion.getDispositivo().getDisponibilidad());
        ArgumentCaptor<HistorialOrden> evento = ArgumentCaptor.forClass(HistorialOrden.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("RETIRO_DISPOSITIVO", evento.getValue().getTipoEvento());
    }

    private OrdenDispositivo asignacion(String estadoOrden) {
        return OrdenDispositivo.builder()
                .idOrdenDispositivo(8L)
                .orden(OrdenTrabajo.builder()
                        .idOrden(7L).codigoOrden("OT-2026-0007")
                        .estado(estadoOrden).build())
                .dispositivo(Dispositivo.builder()
                        .idDispositivo(4L).numeroSerie("SERIE-004")
                        .tipoDispositivo(TipoDispositivo.builder()
                                .nombre("Router").build())
                        .estado("ASIGNADO").disponibilidad(false).build())
                .estadoAsignacion("ASIGNADO")
                .build();
    }
}
