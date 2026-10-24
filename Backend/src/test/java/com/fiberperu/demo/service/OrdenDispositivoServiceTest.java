package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.asignacion.RetirarDispositivoRequest;
import com.fiberperu.demo.entity.Dispositivo;
import com.fiberperu.demo.entity.OrdenDispositivo;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.TipoDispositivo;
import com.fiberperu.demo.repository.DispositivoRepository;
import com.fiberperu.demo.repository.OrdenDispositivoRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenDispositivoServiceTest {

    @Mock private OrdenDispositivoRepository ordenDispositivoRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock private DispositivoRepository dispositivoRepository;
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
    void permiteMarcarInstaladoEnOrdenAbierta() {
        OrdenDispositivo asignacion = asignacion("EN_PROCESO");
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));
        when(ordenDispositivoRepository.save(asignacion)).thenReturn(asignacion);

        var respuesta = service.marcarComoInstalado(8L, "admin@fiberperu.test", true);

        assertEquals("INSTALADO", respuesta.getEstadoAsignacion());
        assertEquals("INSTALADO", asignacion.getDispositivo().getEstado());
    }

    @Test
    void permiteRetirarDispositivoDeOrdenAbierta() {
        OrdenDispositivo asignacion = asignacion("EN_PROCESO");
        when(ordenDispositivoRepository.findById(8L))
                .thenReturn(Optional.of(asignacion));
        when(ordenDispositivoRepository.save(asignacion)).thenReturn(asignacion);

        var respuesta = service.retirar(8L, new RetirarDispositivoRequest(),
                "admin@fiberperu.test", true);

        assertEquals("RETIRADO", respuesta.getEstadoAsignacion());
        assertEquals("EN_STOCK", asignacion.getDispositivo().getEstado());
        assertTrue(asignacion.getDispositivo().getDisponibilidad());
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
