package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.orden.AsignarTecnicoRequest;
import com.fiberperu.demo.dto.orden.CambiarEstadoOrdenRequest;
import com.fiberperu.demo.dto.orden.ProgramarOrdenRequest;
import com.fiberperu.demo.entity.Coordinador;
import com.fiberperu.demo.entity.EvidenciaInstalacion;
import com.fiberperu.demo.entity.OrdenDispositivo;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.CoordinadorRepository;
import com.fiberperu.demo.repository.EvidenciaInstalacionRepository;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.ObservacionServicioRepository;
import com.fiberperu.demo.repository.OrdenDispositivoRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.SolicitudInstalacionRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenTrabajoServiceTest {

    @Mock private OrdenTrabajoRepository ordenRepository;
    @Mock private SolicitudInstalacionRepository solicitudRepository;
    @Mock private CoordinadorRepository coordinadorRepository;
    @Mock private TecnicoRepository tecnicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialOrdenRepository historialRepository;
    @Mock private OrdenDispositivoRepository ordenDispositivoRepository;
    @Mock private EvidenciaInstalacionRepository evidenciaRepository;
    @Mock private ObservacionServicioRepository observacionRepository;
    @InjectMocks private OrdenTrabajoService service;

    @ParameterizedTest
    @ValueSource(strings = {"REGISTRADA", "PROGRAMADA", "ASIGNADA", "OBSERVADA"})
    void noCierraOrdenQueAunNoEstaEnProceso(String estadoActual) {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setEstado(estadoActual);
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(7L, solicitudDeCierre(),
                        "coordinador@fiberperu.test", true));

        assertEquals("La orden debe estar EN_PROCESO antes de finalizarla",
                error.getMessage());
        assertEquals(estadoActual, orden.getEstado());
        verify(ordenRepository, never()).save(any());
        verifyNoInteractions(ordenDispositivoRepository, evidenciaRepository,
                observacionRepository);
    }

    @Test
    void permiteIniciarOrdenProgramadaAntesDelCierre() {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setEstado("PROGRAMADA");
        orden.setFechaProgramada(LocalDate.of(2026, 10, 26));
        orden.setHoraProgramada(LocalTime.of(9, 0));
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(2L).build()));
        when(ordenRepository.save(orden)).thenReturn(orden);

        var respuesta = service.cambiarEstado(7L,
                CambiarEstadoOrdenRequest.builder().estadoNuevo("EN_PROCESO").build(),
                "tecnico@fiberperu.test", false);

        assertEquals("EN_PROCESO", respuesta.getEstado());
        assertNotNull(orden.getFechaInicio());
    }

    @Test
    void noIniciaOrdenSinFechaYHoraProgramadas() {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setEstado("PROGRAMADA");
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(7L,
                        CambiarEstadoOrdenRequest.builder()
                                .estadoNuevo("EN_PROCESO").build(),
                        "tecnico@fiberperu.test", false));

        assertEquals("La orden debe tener fecha y hora programadas antes de iniciar",
                error.getMessage());
        assertEquals("PROGRAMADA", orden.getEstado());
        verify(ordenRepository, never()).save(any());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void noCierraOrdenSinObservacionActivaDelTecnicoAsignado() {
        OrdenTrabajo orden = ordenEnProceso();
        prepararRequisitosExistentes(orden);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(7L, solicitudDeCierre(),
                        "coordinador@fiberperu.test", true));

        assertEquals("El técnico asignado debe registrar al menos una observación antes del cierre",
                error.getMessage());
        verify(observacionRepository)
                .existsByOrdenIdOrdenAndTecnicoIdTecnicoAndEstadoTrue(7L, 2L);
        verify(ordenRepository, never()).save(any());
        assertEquals("EN_PROCESO", orden.getEstado());
    }

    @Test
    void cierraOrdenCuandoTieneObservacionActivaDelTecnicoAsignado() {
        OrdenTrabajo orden = ordenEnProceso();
        prepararRequisitosExistentes(orden);
        when(observacionRepository
                .existsByOrdenIdOrdenAndTecnicoIdTecnicoAndEstadoTrue(7L, 2L))
                .thenReturn(true);
        when(usuarioRepository.findByCorreo("coordinador@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(3L).build()));
        when(ordenRepository.save(orden)).thenReturn(orden);

        var respuesta = service.cambiarEstado(7L, solicitudDeCierre(),
                "coordinador@fiberperu.test", true);

        assertEquals("FINALIZADA", respuesta.getEstado());
        assertNotNull(orden.getFechaFinalizacion());
        verify(ordenRepository).save(orden);
    }

    @Test
    void exigeMotivoCuandoSeCambiaLaFechaOHoraProgramada() {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setFechaProgramada(LocalDate.of(2026, 10, 26));
        orden.setHoraProgramada(LocalTime.of(9, 0));
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.programar(7L, ProgramarOrdenRequest.builder()
                        .fechaProgramada(LocalDate.of(2026, 10, 27))
                        .horaProgramada(LocalTime.of(9, 0))
                        .motivoReprogramacion("   ")
                        .build(), "coordinador@fiberperu.test"));

        assertEquals("El motivo de reprogramación es obligatorio cuando cambia la fecha o la hora",
                error.getMessage());
        verify(ordenRepository, never()).save(any());
        verifyNoInteractions(usuarioRepository, historialRepository);
    }

    @Test
    void guardaMotivoCuandoReprogramaUnaOrden() {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setFechaProgramada(LocalDate.of(2026, 10, 26));
        orden.setHoraProgramada(LocalTime.of(9, 0));
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findByCorreo("coordinador@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(3L).build()));
        when(ordenRepository.save(orden)).thenReturn(orden);

        var respuesta = service.programar(7L, ProgramarOrdenRequest.builder()
                .fechaProgramada(LocalDate.of(2026, 10, 27))
                .horaProgramada(LocalTime.of(10, 0))
                .motivoReprogramacion("  Cliente solicitó otra fecha  ")
                .build(), "coordinador@fiberperu.test");

        assertEquals("Cliente solicitó otra fecha", respuesta.getMotivoReprogramacion());
        verify(ordenRepository).save(orden);
        verify(historialRepository).save(argThat(historial ->
                "REPROGRAMACION".equals(historial.getTipoEvento())
                        && "Cliente solicitó otra fecha".equals(historial.getMotivo())));
    }

    @Test
    void permiteProgramacionInicialSinMotivoDeReprogramacion() {
        OrdenTrabajo orden = ordenEnProceso();
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findByCorreo("coordinador@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(3L).build()));
        when(ordenRepository.save(orden)).thenReturn(orden);

        var respuesta = service.programar(7L, ProgramarOrdenRequest.builder()
                .fechaProgramada(LocalDate.of(2026, 10, 27))
                .horaProgramada(LocalTime.of(10, 0))
                .build(), "coordinador@fiberperu.test");

        assertEquals("PROGRAMADA", respuesta.getEstado());
        assertEquals(LocalDate.of(2026, 10, 27), respuesta.getFechaProgramada());
        assertNull(respuesta.getMotivoReprogramacion());
        verify(historialRepository).save(argThat(historial ->
                "Programación de la instalación".equals(historial.getMotivo())));
    }

    @ParameterizedTest
    @CsvSource({
            "false, true, El técnico no se encuentra activo",
            "true, false, El técnico no se encuentra disponible"
    })
    void noAsignaTecnicoInactivoONoDisponible(
            boolean activo, boolean disponible, String mensajeEsperado) {
        OrdenTrabajo orden = ordenEnProceso();
        orden.setEstado("REGISTRADA");
        orden.setTecnico(null);

        Tecnico tecnico = Tecnico.builder()
                .idTecnico(4L)
                .estado(activo)
                .disponibilidad(disponible)
                .build();

        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));
        when(tecnicoRepository.findById(4L)).thenReturn(Optional.of(tecnico));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> service.asignarTecnico(
                        7L,
                        AsignarTecnicoRequest.builder().idTecnico(4L).build(),
                        "coordinador@fiberperu.test"
                )
        );

        assertEquals(mensajeEsperado, error.getMessage());
        assertEquals("REGISTRADA", orden.getEstado());
        assertNull(orden.getTecnico());
        verify(ordenRepository, never()).save(any());
        verifyNoInteractions(usuarioRepository, historialRepository);
    }

    private void prepararRequisitosExistentes(OrdenTrabajo orden) {
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden));
        when(ordenDispositivoRepository.findByOrdenIdOrden(7L))
                .thenReturn(List.of(OrdenDispositivo.builder()
                        .estadoAsignacion("INSTALADO").build()));
        when(evidenciaRepository.findByOrdenIdOrden(7L))
                .thenReturn(List.of(EvidenciaInstalacion.builder()
                        .estadoValidacion("APROBADA").build()));
    }

    private OrdenTrabajo ordenEnProceso() {
        Usuario coordinadorUsuario = Usuario.builder()
                .nombres("Ana").apellidos("Pérez").build();
        Usuario tecnicoUsuario = Usuario.builder()
                .nombres("Luis").apellidos("Ramos").build();
        return OrdenTrabajo.builder()
                .idOrden(7L)
                .codigoOrden("OT-2026-0007")
                .estado("EN_PROCESO")
                .solicitud(SolicitudInstalacion.builder()
                        .idSolicitud(5L).codigoSolicitud("SOL-2026-0005").build())
                .coordinador(Coordinador.builder()
                        .idCoordinador(3L).usuario(coordinadorUsuario).build())
                .tecnico(Tecnico.builder()
                        .idTecnico(2L).usuario(tecnicoUsuario).build())
                .build();
    }

    private CambiarEstadoOrdenRequest solicitudDeCierre() {
        return CambiarEstadoOrdenRequest.builder()
                .estadoNuevo("FINALIZADA")
                .motivo("Instalación conforme")
                .build();
    }
}
