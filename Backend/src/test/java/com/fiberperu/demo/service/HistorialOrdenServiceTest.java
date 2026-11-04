package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.historial.RegistrarRectificacionRequest;
import com.fiberperu.demo.entity.HistorialOrden;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialOrdenServiceTest {

    @Mock private HistorialOrdenRepository historialRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private HistorialOrdenService service;

    @Test
    void auditoriaGeneralSeConsultaDeMasRecienteAMasAntiguo() {
        when(historialRepository.findAll(any(Sort.class))).thenReturn(List.of());

        service.listarTodos();

        ArgumentCaptor<Sort> orden = ArgumentCaptor.forClass(Sort.class);
        verify(historialRepository).findAll(orden.capture());
        assertEquals(Sort.Direction.DESC,
                orden.getValue().getOrderFor("fechaHora").getDirection());
        assertEquals(Sort.Direction.DESC,
                orden.getValue().getOrderFor("idHistorial").getDirection());
    }

    @Test
    void rectificacionDeOrdenCerradaSeRegistraSinCambiarLaOrden() {
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .idOrden(9L).codigoOrden("OT-2026-1001").estado("FINALIZADA").build();
        Usuario responsable = Usuario.builder()
                .idUsuario(3L).nombres("Juan").apellidos("Perez")
                .correo("coordinador@fiberperu.test").build();
        RegistrarRectificacionRequest request = new RegistrarRectificacionRequest();
        request.setDetalle("  Se rectifica la descripción del servicio.  ");
        when(ordenTrabajoRepository.findById(9L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findByCorreo(responsable.getCorreo()))
                .thenReturn(Optional.of(responsable));
        when(historialRepository.save(any(HistorialOrden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = service.registrarRectificacion(
                9L, request, responsable.getCorreo());

        ArgumentCaptor<HistorialOrden> evento = ArgumentCaptor.forClass(HistorialOrden.class);
        verify(historialRepository).save(evento.capture());
        assertEquals("RECTIFICACION_POSTCIERRE", evento.getValue().getTipoEvento());
        assertEquals("Se rectifica la descripción del servicio.", respuesta.getMotivo());
        assertEquals("FINALIZADA", orden.getEstado());
        verify(ordenTrabajoRepository, never()).save(any(OrdenTrabajo.class));
    }

    @Test
    void noSeRegistraRectificacionSiLaOrdenSigueAbierta() {
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .idOrden(9L).estado("EN_PROCESO").build();
        RegistrarRectificacionRequest request = new RegistrarRectificacionRequest();
        request.setDetalle("Ajuste posterior");
        when(ordenTrabajoRepository.findById(9L)).thenReturn(Optional.of(orden));

        assertThrows(IllegalStateException.class,
                () -> service.registrarRectificacion(9L, request, "admin@fiberperu.test"));
        verify(historialRepository, never()).save(any(HistorialOrden.class));
    }

    @Test
    void noSeRegistraRectificacionSinDetalle() {
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .idOrden(9L).estado("FINALIZADA").build();
        RegistrarRectificacionRequest request = new RegistrarRectificacionRequest();
        request.setDetalle("   ");
        when(ordenTrabajoRepository.findById(9L)).thenReturn(Optional.of(orden));

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarRectificacion(9L, request, "admin@fiberperu.test"));
        verify(historialRepository, never()).save(any(HistorialOrden.class));
    }
}
