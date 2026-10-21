package com.fiberperu.demo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Encapsula Azure Blob Storage para que la URL persistida en PostgreSQL sea
 * generada por el servidor y no enviada arbitrariamente por el navegador.
 */
@Service
public class AzureBlobStorageService {

    private static final long TAMANO_MAXIMO_LECTURA = 10L * 1024 * 1024;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "application/pdf"
    );

    private final String connectionString;
    private final String containerName;

    public AzureBlobStorageService(
            @Value("${azure.storage.connection-string:}") String connectionString,
            @Value("${azure.storage.container-name:evidencias}") String containerName
    ) {
        this.connectionString = connectionString;
        this.containerName = containerName;
    }

    public String almacenarEvidencia(Long idOrden, MultipartFile archivo) {
        validarArchivo(archivo);

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "Azure Blob Storage no está configurado. Defina "
                            + "AZURE_STORAGE_CONNECTION_STRING"
            );
        }

        String contentType = archivo.getContentType().toLowerCase(Locale.ROOT);
        String nombreSeguro = archivo.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String nombreBlob = "ordenes/" + idOrden + "/"
                + UUID.randomUUID() + "-" + nombreSeguro;

        BlobContainerClient contenedor = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);

        contenedor.createIfNotExists();
        BlobClient blob = contenedor.getBlobClient(nombreBlob);

        try (var contenido = archivo.getInputStream()) {
            blob.upload(contenido, archivo.getSize(), true);
            blob.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            return blob.getBlobUrl();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No fue posible leer el archivo de evidencia", exception
            );
        }
    }

    public byte[] leerEvidencia(Long idOrden, String urlArchivo) {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("Azure Blob Storage no está configurado");
        }

        BlobContainerClient contenedor = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);
        String prefijo = contenedor.getBlobContainerUrl() + "/";
        if (urlArchivo == null || !urlArchivo.startsWith(prefijo)) {
            throw new RecursoNoEncontradoException(
                    "Esta evidencia no tiene un archivo disponible en Azure"
            );
        }

        String nombreBlob = URLDecoder.decode(
                urlArchivo.substring(prefijo.length()), StandardCharsets.UTF_8
        );
        if (!nombreBlob.startsWith("ordenes/" + idOrden + "/")
                || nombreBlob.contains("?") || nombreBlob.contains("#")) {
            throw new RecursoNoEncontradoException(
                    "Esta evidencia no tiene un archivo disponible en Azure"
            );
        }

        BlobClient blob = contenedor.getBlobClient(nombreBlob);
        try {
            if (blob.getProperties().getBlobSize() > TAMANO_MAXIMO_LECTURA) {
                throw new IllegalStateException(
                        "El archivo supera el tamaño máximo permitido"
                );
            }
            ByteArrayOutputStream contenido = new ByteArrayOutputStream();
            blob.downloadStream(contenido);
            return contenido.toByteArray();
        } catch (BlobStorageException exception) {
            if (exception.getStatusCode() == 404) {
                throw new RecursoNoEncontradoException(
                        "El archivo de evidencia no se encuentra en Azure"
                );
            }
            throw exception;
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Formato no permitido. Adjunte JPG, PNG o PDF"
            );
        }

        if (archivo.getOriginalFilename() == null || archivo.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("El archivo debe tener un nombre válido");
        }
    }
}
