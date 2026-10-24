package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.orden.OrdenTrabajoResponse;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionResponse;
import com.fiberperu.demo.service.AccesoRecursosService;
import com.fiberperu.demo.service.OrdenTrabajoService;
import com.fiberperu.demo.service.SolicitudInstalacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccesoRecursosControllerTest {

    @Mock private SolicitudInstalacionService solicitudService;
    @Mock private OrdenTrabajoService ordenTrabajoService;
    @Mock private AccesoRecursosService accesoRecursosService;
    @Mock private Authentication authentication;
    @InjectMocks private SolicitudInstalacionController solicitudController;
    @InjectMocks private OrdenTrabajoController ordenController;

    @Test
    void consultaDeSolicitudPorIdCompruebaPropietario() {
        when(solicitudService.buscarResponsePorId(7L))
                .thenReturn(SolicitudInstalacionResponse.builder().idCliente(2L).build());
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoCliente(2L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> solicitudController.buscarPorId(7L, authentication));
    }

    @Test
    void consultaDeSolicitudPorCodigoCompruebaPropietario() {
        when(solicitudService.buscarResponsePorCodigo("SOL-OTRA"))
                .thenReturn(SolicitudInstalacionResponse.builder().idCliente(2L).build());
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoCliente(2L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> solicitudController.buscarPorCodigo("SOL-OTRA", authentication));
    }

    @Test
    void listadoPorClienteCompruebaPropietarioAntesDeConsultar() {
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoCliente(2L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> solicitudController.listarPorCliente(2L, authentication));
        verifyNoInteractions(solicitudService);
    }

    @Test
    void consultaDeOrdenPorIdCompruebaAccesoAntesDeConsultar() {
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoOrden(7L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> ordenController.buscarPorId(7L, authentication));
        verifyNoInteractions(ordenTrabajoService);
    }

    @Test
    void consultaDeOrdenPorCodigoCompruebaAcceso() {
        when(ordenTrabajoService.buscarResponsePorCodigo("OT-OTRA"))
                .thenReturn(OrdenTrabajoResponse.builder().idOrden(7L).build());
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoOrden(7L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> ordenController.buscarPorCodigo("OT-OTRA", authentication));
    }

    @Test
    void cambioDeEstadoCompruebaOrdenAsignadaAntesDeModificar() {
        doThrow(new AccessDeniedException("denegado"))
                .when(accesoRecursosService).verificarAccesoOrden(7L, authentication);

        assertThrows(AccessDeniedException.class,
                () -> ordenController.cambiarEstado(7L, null, authentication));
        verifyNoInteractions(ordenTrabajoService);
    }
}
