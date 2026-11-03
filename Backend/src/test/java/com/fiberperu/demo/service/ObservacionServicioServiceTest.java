package com.fiberperu.demo.service;

import com.fiberperu.demo.entity.ObservacionServicio;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.repository.ObservacionServicioRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservacionServicioServiceTest {

    @Mock private ObservacionServicioRepository observacionRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
    @Mock private TecnicoRepository tecnicoRepository;
    @InjectMocks private ObservacionServicioService service;

    @Test
    void ordenFinalizadaNoPermiteEditarNiDesactivarObservacion() {
        var observacion = ObservacionServicio.builder()
                .idObservacion(3L)
                .orden(OrdenTrabajo.builder().idOrden(6L)
                        .estado("FINALIZADA").build())
                .estado(true)
                .descripcion("Instalación verificada")
                .build();
        when(observacionRepository.findById(3L))
                .thenReturn(Optional.of(observacion));

        var errorEdicion = assertThrows(IllegalStateException.class,
                () -> service.actualizarDescripcion(3L, "Texto cambiado"));
        var errorDesactivacion = assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(3L, false));

        assertEquals("No se pueden modificar observaciones en una orden cerrada",
                errorEdicion.getMessage());
        assertEquals(errorEdicion.getMessage(), errorDesactivacion.getMessage());
        assertEquals("Instalación verificada", observacion.getDescripcion());
        assertEquals(true, observacion.getEstado());
        verify(observacionRepository, never()).save(any());
    }
}
