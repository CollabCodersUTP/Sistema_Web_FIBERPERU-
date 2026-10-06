package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.dispositivo.TipoDispositivoRequest;
import com.fiberperu.demo.dto.dispositivo.TipoDispositivoResponse;
import com.fiberperu.demo.entity.TipoDispositivo;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.TipoDispositivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoDispositivoService {

    private final TipoDispositivoRepository tipoDispositivoRepository;

    /**
     * Lista todos los tipos de dispositivo.
     */
    public List<TipoDispositivoResponse> listarTodos() {
        return tipoDispositivoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un tipo mediante su identificador.
     */
    public TipoDispositivoResponse buscarResponsePorId(
            Long idTipoDispositivo
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idTipoDispositivo)
        );
    }

    /**
     * Busca un tipo mediante su nombre.
     */
    public TipoDispositivoResponse buscarResponsePorNombre(
            String nombre
    ) {
        String nombreNormalizado = normalizarNombre(nombre);

        TipoDispositivo tipo = tipoDispositivoRepository
                .findByNombre(nombreNormalizado)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tipo de dispositivo no encontrado: "
                                        + nombre
                        )
                );

        return convertirAResponse(tipo);
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public TipoDispositivo buscarEntidadPorId(
            Long idTipoDispositivo
    ) {
        return tipoDispositivoRepository
                .findById(idTipoDispositivo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tipo de dispositivo no encontrado con id: "
                                        + idTipoDispositivo
                        )
                );
    }

    /**
     * Registra un nuevo tipo de dispositivo.
     */
    @Transactional
    public TipoDispositivoResponse registrar(
            TipoDispositivoRequest request
    ) {
        String nombre = normalizarNombre(
                request.getNombre()
        );

        if (tipoDispositivoRepository
                .findByNombre(nombre)
                .isPresent()) {
            throw new IllegalArgumentException(
                    "El tipo de dispositivo ya está registrado"
            );
        }

        TipoDispositivo tipo = TipoDispositivo.builder()
                .nombre(nombre)
                .descripcion(
                        normalizarTextoOpcional(
                                request.getDescripcion()
                        )
                )
                .estado(true)
                .build();

        TipoDispositivo guardado =
                tipoDispositivoRepository.save(tipo);

        return convertirAResponse(guardado);
    }

    /**
     * Actualiza el nombre y la descripción.
     */
    @Transactional
    public TipoDispositivoResponse actualizar(
            Long idTipoDispositivo,
            TipoDispositivoRequest request
    ) {
        TipoDispositivo tipo =
                buscarEntidadPorId(idTipoDispositivo);

        String nombreNuevo =
                normalizarNombre(request.getNombre());

        tipoDispositivoRepository
                .findByNombre(nombreNuevo)
                .filter(encontrado ->
                        !encontrado.getIdTipoDispositivo()
                                .equals(idTipoDispositivo)
                )
                .ifPresent(encontrado -> {
                    throw new IllegalArgumentException(
                            "El nombre del tipo ya está registrado"
                    );
                });

        tipo.setNombre(nombreNuevo);
        tipo.setDescripcion(
                normalizarTextoOpcional(
                        request.getDescripcion()
                )
        );

        TipoDispositivo actualizado =
                tipoDispositivoRepository.save(tipo);

        return convertirAResponse(actualizado);
    }

    /**
     * Activa o desactiva el tipo de dispositivo.
     */
    @Transactional
    public TipoDispositivoResponse cambiarEstado(
            Long idTipoDispositivo,
            boolean estado
    ) {
        TipoDispositivo tipo =
                buscarEntidadPorId(idTipoDispositivo);

        tipo.setEstado(estado);

        TipoDispositivo actualizado =
                tipoDispositivoRepository.save(tipo);

        return convertirAResponse(actualizado);
    }

    /**
     * Convierte la entidad en un DTO.
     */
    public TipoDispositivoResponse convertirAResponse(
            TipoDispositivo tipo
    ) {
        return TipoDispositivoResponse.builder()
                .idTipoDispositivo(
                        tipo.getIdTipoDispositivo()
                )
                .nombre(tipo.getNombre())
                .descripcion(tipo.getDescripcion())
                .estado(tipo.getEstado())
                .build();
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");
    }

    private String normalizarTextoOpcional(
            String texto
    ) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}