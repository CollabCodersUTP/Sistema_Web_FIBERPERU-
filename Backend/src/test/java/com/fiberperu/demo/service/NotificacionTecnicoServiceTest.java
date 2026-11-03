package com.fiberperu.demo.service;

import com.fiberperu.demo.entity.NotificacionTecnico;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.NotificacionTecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionTecnicoServiceTest {
    @Mock private NotificacionTecnicoRepository notificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private NotificacionTecnicoService service;

    @Test
    void permiteMarcarComoLeidoSoloUnAvisoPropio() {
        Usuario tecnico = Usuario.builder().idUsuario(8L).build();
        NotificacionTecnico aviso = NotificacionTecnico.builder()
                .idNotificacion(5L).usuario(tecnico)
                .orden(OrdenTrabajo.builder().idOrden(7L).codigoOrden("OT-2026-0007").build())
                .tipoEvento("ASIGNACION").mensaje("Se te asignó la orden.").build();
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(tecnico));
        when(notificacionRepository.findByIdNotificacionAndUsuarioIdUsuario(5L, 8L))
                .thenReturn(Optional.of(aviso));

        var respuesta = service.marcarLeida(5L, "tecnico@fiberperu.test");

        assertNotNull(respuesta.fechaLectura());
        verify(notificacionRepository).save(aviso);
    }

    @Test
    void noPermiteLeerElAvisoDeOtroTecnico() {
        when(usuarioRepository.findByCorreo("tecnico@fiberperu.test"))
                .thenReturn(Optional.of(Usuario.builder().idUsuario(8L).build()));
        when(notificacionRepository.findByIdNotificacionAndUsuarioIdUsuario(5L, 8L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.marcarLeida(5L, "tecnico@fiberperu.test"));
        verify(notificacionRepository, never()).save(any());
    }
}
