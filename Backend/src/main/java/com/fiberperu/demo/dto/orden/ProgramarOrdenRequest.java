package com.fiberperu.demo.dto.orden;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramarOrdenRequest {

    @NotNull(message = "La fecha programada es obligatoria")
    @FutureOrPresent(
            message = "La fecha programada no puede estar en el pasado"
    )
    private LocalDate fechaProgramada;

    @NotNull(message = "La hora programada es obligatoria")
    private LocalTime horaProgramada;
}