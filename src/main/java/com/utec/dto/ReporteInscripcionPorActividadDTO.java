package com.utec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteInscripcionPorActividadDTO {
    private Integer idActividad;
    private String nombreActividad;
    private String tipoActividad;
    private Integer cantidadInscripciones;
    private Integer cantidadCancelaciones;
}
