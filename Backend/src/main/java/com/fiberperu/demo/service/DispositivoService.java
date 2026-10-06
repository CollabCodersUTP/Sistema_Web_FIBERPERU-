package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.dispositivo.DispositivoRequest;
import com.fiberperu.demo.dto.dispositivo.DispositivoResponse;
import com.fiberperu.demo.entity.Dispositivo;
import com.fiberperu.demo.entity.TipoDispositivo;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.DispositivoRepository;
import com.fiberperu.demo.repository.TipoDispositivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispositivoService {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "EN_STOCK", "ASIGNADO", "INSTALADO", "RETIRADO",
            "DEFECTUOSO", "INACTIVO"
    );

    private final DispositivoRepository dispositivoRepository;
    private final TipoDispositivoRepository tipoDispositivoRepository;

    public List<DispositivoResponse> listarTodos() {
        return dispositivoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<DispositivoResponse> listarDisponibles() {
        return dispositivoRepository.findByDisponibilidadTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public DispositivoResponse buscarResponsePorId(Long id) {
        return convertirAResponse(buscarEntidadPorId(id));
    }

    public Dispositivo buscarEntidadPorId(Long id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Dispositivo no encontrado con id: " + id
                        )
                );
    }

    @Transactional
    public DispositivoResponse registrar(
            DispositivoRequest request
    ) {
        if (dispositivoRepository.existsByNumeroSerie(
                request.getNumeroSerie()
        )) {
            throw new IllegalArgumentException(
                    "El número de serie ya está registrado"
            );
        }

        TipoDispositivo tipoDispositivo =
                tipoDispositivoRepository.findById(
                        request.getIdTipoDispositivo()
                ).orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tipo de dispositivo no encontrado con id: "
                                        + request.getIdTipoDispositivo()
                        )
                );

        Dispositivo dispositivo = Dispositivo.builder()
                .tipoDispositivo(tipoDispositivo)
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .numeroSerie(request.getNumeroSerie())
                .estado("EN_STOCK")
                .disponibilidad(true)
                .build();

        Dispositivo guardado =
                dispositivoRepository.save(dispositivo);

        return convertirAResponse(guardado);
    }

    @Transactional
    public DispositivoResponse cambiarDisponibilidad(
            Long id,
            boolean disponibilidad
    ) {
        Dispositivo dispositivo = buscarEntidadPorId(id);

        dispositivo.setDisponibilidad(disponibilidad);

        if (disponibilidad
                && "ASIGNADO".equals(dispositivo.getEstado())) {
            dispositivo.setEstado("EN_STOCK");
        }

        Dispositivo actualizado =
                dispositivoRepository.save(dispositivo);

        return convertirAResponse(actualizado);
    }

    @Transactional
    public DispositivoResponse actualizar(
            Long id,
            DispositivoRequest request
    ) {
        Dispositivo dispositivo = buscarEntidadPorId(id);

        dispositivoRepository.findByNumeroSerie(request.getNumeroSerie().trim())
                .filter(existente -> !existente.getIdDispositivo().equals(id))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException(
                            "El número de serie ya está registrado"
                    );
                });

        TipoDispositivo tipo = tipoDispositivoRepository.findById(
                request.getIdTipoDispositivo()
        ).orElseThrow(() -> new RecursoNoEncontradoException(
                "Tipo de dispositivo no encontrado con id: "
                        + request.getIdTipoDispositivo()
        ));

        if (!Boolean.TRUE.equals(tipo.getEstado())) {
            throw new IllegalStateException(
                    "No se puede asignar un tipo de dispositivo inactivo"
            );
        }

        dispositivo.setTipoDispositivo(tipo);
        dispositivo.setMarca(request.getMarca().trim());
        dispositivo.setModelo(request.getModelo().trim());
        dispositivo.setNumeroSerie(request.getNumeroSerie().trim());

        return convertirAResponse(dispositivoRepository.save(dispositivo));
    }

    @Transactional
    public DispositivoResponse cambiarEstado(Long id, String nuevoEstado) {
        Dispositivo dispositivo = buscarEntidadPorId(id);
        String estado = nuevoEstado.trim().toUpperCase(Locale.ROOT);

        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            throw new IllegalArgumentException(
                    "Estado de dispositivo no permitido: " + estado
            );
        }

        dispositivo.setEstado(estado);
        dispositivo.setDisponibilidad("EN_STOCK".equals(estado));

        return convertirAResponse(dispositivoRepository.save(dispositivo));
    }

    private DispositivoResponse convertirAResponse(
            Dispositivo dispositivo
    ) {
        TipoDispositivo tipo =
                dispositivo.getTipoDispositivo();

        return DispositivoResponse.builder()
                .idDispositivo(dispositivo.getIdDispositivo())
                .idTipoDispositivo(
                        tipo.getIdTipoDispositivo()
                )
                .tipoDispositivo(tipo.getNombre())
                .marca(dispositivo.getMarca())
                .modelo(dispositivo.getModelo())
                .numeroSerie(dispositivo.getNumeroSerie())
                .estado(dispositivo.getEstado())
                .disponibilidad(dispositivo.getDisponibilidad())
                .fechaRegistro(dispositivo.getFechaRegistro())
                .build();
    }
}
