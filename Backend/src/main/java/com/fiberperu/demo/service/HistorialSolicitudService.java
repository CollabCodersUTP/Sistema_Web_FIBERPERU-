package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.historial.HistorialSolicitudResponse;
import com.fiberperu.demo.entity.HistorialSolicitud;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.HistorialSolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialSolicitudService {
    private final HistorialSolicitudRepository historialRepository;

    public List<HistorialSolicitudResponse> listarTodos() {
        return historialRepository.findAll(Sort.by(
                        Sort.Order.desc("fechaHora"),
                        Sort.Order.desc("idHistorialSolicitud")))
                .stream()
                .map(this::convertir)
                .toList();
    }

    private HistorialSolicitudResponse convertir(HistorialSolicitud historial) {
        Usuario usuario = historial.getUsuario();
        return HistorialSolicitudResponse.builder()
                .idHistorialSolicitud(historial.getIdHistorialSolicitud())
                .idSolicitud(historial.getSolicitud().getIdSolicitud())
                .codigoSolicitud(historial.getSolicitud().getCodigoSolicitud())
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombres() + " " + usuario.getApellidos())
                .tipoEvento(historial.getTipoEvento())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .fechaHora(historial.getFechaHora())
                .motivo(historial.getMotivo())
                .build();
    }
}
