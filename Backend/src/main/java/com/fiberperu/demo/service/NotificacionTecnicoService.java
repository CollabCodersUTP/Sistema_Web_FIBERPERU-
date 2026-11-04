package com.fiberperu.demo.service;

import com.fiberperu.demo.util.HoraPeru;

import com.fiberperu.demo.dto.notificacion.NotificacionTecnicoResponse;
import com.fiberperu.demo.entity.NotificacionTecnico;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.NotificacionTecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionTecnicoService {

    private final NotificacionTecnicoRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public List<NotificacionTecnicoResponse> listarMias(String correo) {
        Long idUsuario = buscarUsuario(correo).getIdUsuario();
        return notificacionRepository
                .findByUsuarioIdUsuarioOrderByFechaCreacionDescIdNotificacionDesc(
                        idUsuario, PageRequest.of(0, 50))
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional
    public NotificacionTecnicoResponse marcarLeida(Long id, String correo) {
        Long idUsuario = buscarUsuario(correo).getIdUsuario();
        NotificacionTecnico notificacion = notificacionRepository
                .findByIdNotificacionAndUsuarioIdUsuario(id, idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Aviso no encontrado"));
        if (notificacion.getFechaLectura() == null) {
            notificacion.setFechaLectura(HoraPeru.ahora());
            notificacionRepository.save(notificacion);
        }
        return convertir(notificacion);
    }

    @Transactional
    public void registrarAsignacion(OrdenTrabajo orden) {
        registrar(orden, "ASIGNACION",
                "Se te asignó la orden " + orden.getCodigoOrden() + ".");
    }

    @Transactional
    public void registrarReprogramacion(OrdenTrabajo orden) {
        registrar(orden, "REPROGRAMACION",
                "Se reprogramó la orden " + orden.getCodigoOrden() + " para el "
                        + orden.getFechaProgramada() + " a las " + orden.getHoraProgramada() + ".");
    }

    private void registrar(OrdenTrabajo orden, String tipo, String mensaje) {
        if (orden.getTecnico() == null) {
            return;
        }
        notificacionRepository.save(NotificacionTecnico.builder()
                .usuario(orden.getTecnico().getUsuario())
                .orden(orden)
                .tipoEvento(tipo)
                .mensaje(mensaje)
                .build());
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado"));
    }

    private NotificacionTecnicoResponse convertir(NotificacionTecnico notificacion) {
        return new NotificacionTecnicoResponse(
                notificacion.getIdNotificacion(),
                notificacion.getOrden().getIdOrden(),
                notificacion.getOrden().getCodigoOrden(),
                notificacion.getTipoEvento(),
                notificacion.getMensaje(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaLectura());
    }
}
