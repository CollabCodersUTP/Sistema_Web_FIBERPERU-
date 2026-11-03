package com.fiberperu.demo.dto.notificacion;

import java.time.LocalDateTime;

public record NotificacionTecnicoResponse(
        Long idNotificacion,
        Long idOrden,
        String codigoOrden,
        String tipoEvento,
        String mensaje,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaLectura
) {}
