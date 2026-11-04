package com.fiberperu.demo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class HoraPeru {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private HoraPeru() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA);
    }
}
