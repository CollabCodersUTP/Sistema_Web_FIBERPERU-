package com.fiberperu.demo.service;

import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialOrdenServiceTest {

    @Mock private HistorialOrdenRepository historialRepository;
    @Mock private OrdenTrabajoRepository ordenTrabajoRepository;
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
}
