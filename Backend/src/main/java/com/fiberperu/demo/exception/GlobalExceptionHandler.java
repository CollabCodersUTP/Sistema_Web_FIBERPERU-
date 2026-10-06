package com.fiberperu.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Recurso inexistente.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Datos inválidos enviados por el cliente.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> manejarArgumentoInvalido(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Operación no permitida por una regla de negocio.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> manejarEstadoInvalido(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Credenciales incorrectas durante el inicio de sesión.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> manejarCredencialesIncorrectas(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Usuario autenticado sin permisos suficientes.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> manejarAccesoDenegado(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operación",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Errores producidos por @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            errores.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request.getRequestURI(),
                errores
        );
    }

    /**
     * Violaciones de restricciones UNIQUE, FK o CHECK.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarIntegridadDatos(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.CONFLICT,
                "La operación incumple una restricción de integridad",
                request.getRequestURI(),
                null
        );
    }

    /**
     * JSON incompleto o con sintaxis incorrecta.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no tiene un formato válido",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Parámetros obligatorios ausentes.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> manejarParametroFaltante(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio: "
                        + exception.getParameterName(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Tipo incorrecto en parámetros o identificadores.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "El valor proporcionado para '"
                        + exception.getName()
                        + "' no tiene el formato esperado",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Error no contemplado.
     *
     * No devuelve detalles internos, consultas SQL
     * ni trazas del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> construirRespuesta(
            HttpStatus estado,
            String mensaje,
            String ruta,
            Map<String, String> erroresValidacion
    ) {
        ApiErrorResponse respuesta =
                ApiErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(estado.value())
                        .error(estado.getReasonPhrase())
                        .message(mensaje)
                        .path(ruta)
                        .validationErrors(erroresValidacion)
                        .build();

        return ResponseEntity
                .status(estado)
                .body(respuesta);
    }
}