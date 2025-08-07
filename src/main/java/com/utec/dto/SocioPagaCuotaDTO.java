package com.utec.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.utec.model.Cuota;
import com.utec.model.Usuario;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SocioPagaCuotaDTO {

    @NotNull(message = "El campo de id usuario es obligatorio.")
    @Min(value = 1, message = "Ingrese un numero igual o mayor a 1")
    @Max(value = 2147483647, message = "El id no puede superar 2.147.483.647")
    private Integer idUsuario;

    @NotNull(message = "El campo de id cuota es obligatorio.")
    @Min(value = 1, message = "Ingrese un numero igual o mayor a 1")
    @Max(value = 2147483647, message = "El id no puede superar 2.147.483.647")
    private Integer idCuota;

    @NotNull(message = "El campo de fecha de cobro es obligatorio.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "La fecha de cobro no puede ser futura")
    private LocalDate fechaCobro;

    @NotNull(message = "El campo de modo pago es obligatorio.")
    @Min(value = 1, message = "Ingrese un numero igual o mayor a 1")
    @Max(value = 2147483647, message = "El id no puede superar 2.147.483.647")
    private Integer modoPago;

}
