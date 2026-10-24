package com.fiberperu.demo.service;

import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccesoRecursosServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private OrdenTrabajoRepository ordenRepository;
    @InjectMocks private AccesoRecursosService accesoRecursosService;

    @Test
    void clientePuedeConsultarSusPropiosRecursos() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente("ana@fiberperu.test")));
        Authentication ana = usuario("ana@fiberperu.test", "CLIENTE");

        assertDoesNotThrow(() -> accesoRecursosService.verificarAccesoCliente(1L, ana));
    }

    @Test
    void clienteNoPuedeConsultarRecursosDeOtroCliente() {
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente("luis@fiberperu.test")));
        Authentication ana = usuario("ana@fiberperu.test", "CLIENTE");

        assertThrows(AccessDeniedException.class,
                () -> accesoRecursosService.verificarAccesoCliente(2L, ana));
    }

    @Test
    void tecnicoNoPuedeConsultarSolicitudesDeClientes() {
        assertThrows(AccessDeniedException.class,
                () -> accesoRecursosService.verificarAccesoCliente(
                        1L, usuario("tecnico@fiberperu.test", "TECNICO")));
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void administradorPuedeConsultarCualquierCliente() {
        assertDoesNotThrow(() -> accesoRecursosService.verificarAccesoCliente(
                2L, usuario("admin@fiberperu.test", "ADMINISTRADOR")));
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void clienteNoPuedeConsultarOrdenDeOtroCliente() {
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden(
                "luis@fiberperu.test", "tecnico@fiberperu.test")));

        assertThrows(AccessDeniedException.class,
                () -> accesoRecursosService.verificarAccesoOrden(
                        7L, usuario("ana@fiberperu.test", "CLIENTE")));
    }

    @Test
    void clientePuedeConsultarSuOrden() {
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden(
                "ana@fiberperu.test", "tecnico@fiberperu.test")));

        assertDoesNotThrow(() -> accesoRecursosService.verificarAccesoOrden(
                7L, usuario("ana@fiberperu.test", "CLIENTE")));
    }

    @Test
    void tecnicoSoloPuedeAccederAOrdenAsignada() {
        when(ordenRepository.findById(7L)).thenReturn(Optional.of(orden(
                "ana@fiberperu.test", "tecnico@fiberperu.test")));
        Authentication otroTecnico = usuario("otro@fiberperu.test", "TECNICO");

        assertThrows(AccessDeniedException.class,
                () -> accesoRecursosService.verificarAccesoOrden(7L, otroTecnico));
        assertDoesNotThrow(() -> accesoRecursosService.verificarAccesoOrden(
                7L, usuario("tecnico@fiberperu.test", "TECNICO")));
    }

    private static Authentication usuario(String correo, String rol) {
        return new UsernamePasswordAuthenticationToken(
                correo, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    private static Cliente cliente(String correo) {
        return Cliente.builder().usuario(Usuario.builder().correo(correo).build()).build();
    }

    private static OrdenTrabajo orden(String correoCliente, String correoTecnico) {
        return OrdenTrabajo.builder()
                .solicitud(SolicitudInstalacion.builder()
                        .cliente(cliente(correoCliente)).build())
                .tecnico(Tecnico.builder()
                        .usuario(Usuario.builder().correo(correoTecnico).build()).build())
                .build();
    }
}
