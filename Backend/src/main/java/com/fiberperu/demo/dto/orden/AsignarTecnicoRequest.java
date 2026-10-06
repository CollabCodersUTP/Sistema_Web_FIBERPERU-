package com.fiberperu.demo.dto.orden;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarTecnicoRequest {

    @NotNull(message = "El técnico es obligatorio")
    private Long idTecnico;
}