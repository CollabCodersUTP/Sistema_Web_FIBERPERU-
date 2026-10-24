package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionRequest;
import com.fiberperu.demo.dto.evidencia.ValidarEvidenciaRequest;
import com.fiberperu.demo.entity.EvidenciaInstalacion;
import com.fiberperu.demo.entity.Coordinador;
import com.fiberperu.demo.entity.HistorialOrden;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.CoordinadorRepository;
import com.fiberperu.demo.repository.EvidenciaInstalacionRepository;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenciaInstalacionServiceTest {

    @Mock private EvidenciaInstalacionRepository evidenciaRepository;
    @Mock private HistorialOrdenRepository historialRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CoordinadorRepository coordinadorRepository;
    @Mock private TecnicoRepository tecnicoRepository;
    @Mock private AzureBlobStorageService azureBlobStorageService;
    @InjectMocks private EvidenciaInstalacionService service;

    @Test
    void administradorPuedeLeerArchivoDeLaOrden() {
        var evidencia = evidencia();
        when(evidenciaRepository.findById(3L)).thenReturn(Optional.of(evidencia));
        when(azureBlobStorageService.leerEvidencia(6L, evidencia.getUrlArchivo()))
                .thenReturn(new byte[]{1, 2, 3});

        var archivo = service.obtenerArchivo(3L, "admin@fiberperu.test", true);

        assertArrayEquals(new byte[]{1, 2, 3}, archivo.contenido());
    }

    @Test
    void tecnicoAjenoNoPuedeLeerArchivo() {
        when(evidenciaRepository.findById(3L)).thenReturn(Optional.of(evidencia()));
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(4L).build()));
        when(tecnicoRepository.findByUsuarioIdUsuario(4L))
                .thenReturn(Optional.of(Tecnico.builder().idTecnico(9L).build()));

        assertThrows(AccessDeniedException.class, () ->
                service.obtenerArchivo(3L, "tecnico@fiberperu.test", false));
        verifyNoInteractions(azureBlobStorageService);
    }

    @Test
    void tipoNuevoSeGuardaEnMinusculas() {
        when(ordenTrabajoRepository.findById(6L))
                .thenReturn(Optional.of(OrdenTrabajo.builder()
                        .idOrden(6L).estado("REGISTRADA")
                        .tecnico(Tecnico.builder().idTecnico(2L).build())
                        .build()));
        when(evidenciaRepository.save(any(EvidenciaInstalacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var request = EvidenciaInstalacionRequest.builder()
                .idOrden(6L)
                .nombreArchivo("prueba.png")
                .tipoArchivo("IMAGE/PNG")
                .urlArchivo("https://example.test/prueba.png")
                .build();

        var response = service.registrar(request);

        assertEquals("image/png", response.getTipoArchivo());
    }

    @Test
    void tecnicoAsignadoReenviaEvidenciaObservadaParaNuevaRevision() {
        var evidencia = evidenciaObservada("EN_PROCESO");
        var archivo = new MockMultipartFile("archivo", "corregida.png",
                "image/png", new byte[]{1, 2, 3});
        when(evidenciaRepository.findById(3L)).thenReturn(Optional.of(evidencia));
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(4L).build()));
        when(tecnicoRepository.findByUsuarioIdUsuario(4L))
                .thenReturn(Optional.of(Tecnico.builder().idTecnico(2L).build()));
        when(azureBlobStorageService.almacenarEvidencia(6L, archivo))
                .thenReturn("https://example.test/corregida.png");
        when(evidenciaRepository.save(any(EvidenciaInstalacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = service.reenviarArchivo(3L, archivo, "Instalación corregida",
                "tecnico@fiberperu.test", false);

        assertEquals("PENDIENTE", respuesta.getEstadoValidacion());
        assertEquals("corregida.png", respuesta.getNombreArchivo());
        assertEquals("https://example.test/corregida.png", respuesta.getUrlArchivo());
        assertEquals("Instalación corregida", respuesta.getDescripcion());
        assertEquals("Foto borrosa", respuesta.getObservacionValidacion());
        assertNull(respuesta.getFechaValidacion());
        assertNull(respuesta.getIdCoordinadorValidador());

        ArgumentCaptor<HistorialOrden> historial = ArgumentCaptor.forClass(HistorialOrden.class);
        verify(historialRepository, times(2)).save(historial.capture());
        List<HistorialOrden> eventos = historial.getAllValues();
        assertEquals("VALIDACION_EVIDENCIA", eventos.get(0).getTipoEvento());
        assertEquals("Foto borrosa", eventos.get(0).getMotivo());
        assertEquals("REENVIO_EVIDENCIA", eventos.get(1).getTipoEvento());
        assertEquals("OBSERVADA", eventos.get(1).getEstadoAnterior());
        assertEquals("PENDIENTE", eventos.get(1).getEstadoNuevo());
    }

    @Test
    void coordinadorPuedeAprobarLaMismaEvidenciaDespuesDelReenvio() {
        var evidencia = evidenciaObservada("EN_PROCESO");
        var archivo = new MockMultipartFile("archivo", "corregida.png",
                "image/png", new byte[]{1, 2, 3});
        when(evidenciaRepository.findById(3L)).thenReturn(Optional.of(evidencia));
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(4L).build()));
        when(tecnicoRepository.findByUsuarioIdUsuario(4L))
                .thenReturn(Optional.of(Tecnico.builder().idTecnico(2L).build()));
        when(azureBlobStorageService.almacenarEvidencia(6L, archivo))
                .thenReturn("https://example.test/corregida.png");
        when(evidenciaRepository.save(any(EvidenciaInstalacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findByCorreo("coordinador@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(8L).build()));
        when(coordinadorRepository.findByUsuarioIdUsuario(8L))
                .thenReturn(Optional.of(Coordinador.builder().idCoordinador(9L)
                        .estado(true).usuario(Usuario.builder().idUsuario(8L)
                                .nombres("Ana").apellidos("Coordinadora").build())
                        .build()));

        service.reenviarArchivo(3L, archivo, null, "tecnico@fiberperu.test", false);
        var respuesta = service.validar(3L, ValidarEvidenciaRequest.builder()
                .estadoValidacion("APROBADA").build(), "coordinador@fiberperu.test");

        assertEquals(3L, respuesta.getIdEvidencia());
        assertEquals("APROBADA", respuesta.getEstadoValidacion());
        assertEquals("corregida.png", respuesta.getNombreArchivo());
        assertEquals(9L, respuesta.getIdCoordinadorValidador());
    }

    @Test
    void tecnicoAjenoNoPuedeReenviarEvidenciaObservada() {
        when(evidenciaRepository.findById(3L))
                .thenReturn(Optional.of(evidenciaObservada("EN_PROCESO")));
        when(usuarioRepository.findByCorreo("otro@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(9L).build()));
        when(tecnicoRepository.findByUsuarioIdUsuario(9L))
                .thenReturn(Optional.of(Tecnico.builder().idTecnico(8L).build()));

        assertThrows(IllegalStateException.class, () -> service.reenviarArchivo(
                3L, new MockMultipartFile("archivo", "corregida.png", "image/png", new byte[]{1}),
                null, "otro@fiberperu.test", false));

        verifyNoInteractions(azureBlobStorageService, historialRepository);
    }

    @Test
    void evidenciaNoObservadaNoPuedeReenviarse() {
        var evidencia = evidenciaObservada("EN_PROCESO");
        evidencia.setEstadoValidacion("APROBADA");
        when(evidenciaRepository.findById(3L)).thenReturn(Optional.of(evidencia));

        assertThrows(IllegalStateException.class, () -> service.reenviarArchivo(
                3L, new MockMultipartFile("archivo", "corregida.png", "image/png", new byte[]{1}),
                null, "tecnico@fiberperu.test", false));

        verifyNoInteractions(azureBlobStorageService, historialRepository);
    }

    @Test
    void ordenFinalizadaNoPermiteReenviarEvidencia() {
        when(evidenciaRepository.findById(3L))
                .thenReturn(Optional.of(evidenciaObservada("FINALIZADA")));

        assertThrows(IllegalStateException.class, () -> service.reenviarArchivo(
                3L, new MockMultipartFile("archivo", "corregida.png", "image/png", new byte[]{1}),
                null, "tecnico@fiberperu.test", false));

        verifyNoInteractions(azureBlobStorageService, historialRepository);
    }

    private EvidenciaInstalacion evidencia() {
        var orden = OrdenTrabajo.builder()
                .idOrden(6L)
                .tecnico(Tecnico.builder().idTecnico(2L).build())
                .build();
        return EvidenciaInstalacion.builder()
                .orden(orden)
                .urlArchivo("https://example.test/evidencia.png")
                .nombreArchivo("evidencia.png")
                .tipoArchivo("image/png")
                .build();
    }

    private EvidenciaInstalacion evidenciaObservada(String estadoOrden) {
        var orden = OrdenTrabajo.builder()
                .idOrden(6L).codigoOrden("OT-2026-0001")
                .estado(estadoOrden)
                .tecnico(Tecnico.builder().idTecnico(2L).build())
                .build();
        return EvidenciaInstalacion.builder()
                .idEvidencia(3L)
                .orden(orden)
                .coordinadorValidador(Coordinador.builder().idCoordinador(5L)
                        .usuario(Usuario.builder().idUsuario(7L).build()).build())
                .nombreArchivo("borrosa.png")
                .tipoArchivo("image/png")
                .urlArchivo("https://example.test/borrosa.png")
                .fechaValidacion(LocalDateTime.of(2026, 10, 5, 10, 0))
                .observacionValidacion("Foto borrosa")
                .estadoValidacion("OBSERVADA")
                .build();
    }
}
