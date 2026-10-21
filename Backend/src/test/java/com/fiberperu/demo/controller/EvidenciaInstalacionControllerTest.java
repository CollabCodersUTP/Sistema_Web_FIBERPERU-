package com.fiberperu.demo.controller;

import com.fiberperu.demo.service.EvidenciaInstalacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenciaInstalacionControllerTest {

    @Mock private EvidenciaInstalacionService evidenciaService;
    @Mock private Authentication authentication;
    @InjectMocks private EvidenciaInstalacionController controller;

    @Test
    void tipoGuardadoEnMayusculasSeEntregaComoImagen() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        when(authentication.getName()).thenReturn("admin@fiberperu.test");
        when(evidenciaService.obtenerArchivo(3L, "admin@fiberperu.test", true))
                .thenReturn(new EvidenciaInstalacionService.ArchivoEvidencia(
                        new byte[]{1, 2, 3}, "prueba.png", "IMAGE/PNG"));

        var response = controller.verArchivo(3L, authentication);

        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, response.getBody());
    }
}
