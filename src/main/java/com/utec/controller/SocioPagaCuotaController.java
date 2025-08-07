package com.utec.controller;

import com.utec.dto.ActividadDTO;
import com.utec.dto.SocioPagaCuotaDTO;
import com.utec.service.SocioPagaCuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos/cuotas")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Contolador de pagos cuotas del socio", description = "Gestion de pagos de cuotas del socio")
public class SocioPagaCuotaController {

    @Autowired
    private SocioPagaCuotaService socioPagaCuotaService;

    // post/pagos
    @PostMapping()
    @Operation(
            summary = "Pagar la cuota de un socio",
            description = "Metodo que paga la cuota de un socio. Solo un usuario con rol administrador podra realizar el pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago registrado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActividadDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    public ResponseEntity<String> registrarPago(@Valid @RequestBody SocioPagaCuotaDTO dto) {
        socioPagaCuotaService.registrarPago(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Pago registrado correctamente.");
    }

    //actualizar el pago
    @PatchMapping()
    @Operation(
            summary = "Modificar el pago de una cuota de un socio",
            description = "Permite modificar la fecha y el modo de pago de una cuota ya pagada por el socio. Solo un usuario con rol administrador podra realizar la modificacion del pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActividadDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    public ResponseEntity<String> modificarPago(@Valid @RequestBody SocioPagaCuotaDTO dto) {
        socioPagaCuotaService.modificarPago(dto);
        return ResponseEntity.ok("Pago actualizado correctamente.");
    }

    //listar informacion de pagos
    @GetMapping()
    @Operation(
            summary = "Obtener listado de pagos de cuota de socios",
            description = "Retorna una lista de todos los pagos de socios registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pagos de cuotas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActividadDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "No hay pagos para mostrar"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> obtenerPagoCuotas() {
        try {
            List<SocioPagaCuotaDTO> lista = socioPagaCuotaService.ObtenerPagosCuotas();
            if (lista.isEmpty()) {
                return ResponseEntity.badRequest().body("No hay pagos para mostrar.");
            }
            return new ResponseEntity<>(lista, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al obtener pagos: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
