package com.fiberperu.demo.service;

import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureBlobStorageServiceTest {

    @Test
    void rechazaUrlAjenaAlContenedorConfigurado() {
        var storage = new AzureBlobStorageService(
                "UseDevelopmentStorage=true", "evidencias"
        );

        assertThrows(RecursoNoEncontradoException.class, () ->
                storage.leerEvidencia(
                        6L, "https://storage.example.test/evidencias/evidencia_demo.jpg"
                ));
    }
}
