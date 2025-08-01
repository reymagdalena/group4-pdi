package com.utec.controller;

import com.utec.dto.EstadoDTO;
import com.utec.mapper.EstadoMapper;
import com.utec.model.Estado;
import com.utec.model.Usuario;
import com.utec.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Controlador de Estados", description = "Gestión de estados del sistema (activo, inactivo, pendiente, etc.).")
public class EstadoController {

    @Autowired
    EstadoService estadoService;

    @GetMapping("/estados")
    @Operation(
            summary = "Obtener todos los estados",
            description = "Retorna una lista de todos los estados registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de estados obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "No hay estados para mostrar"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> obtenerEstados(){
        try{
            List<EstadoDTO> estados = estadoService.obtenerEstados();
            if (estados.isEmpty()) {
                return ResponseEntity.badRequest().body("No hay estados para mostrar.");
            }
            return new ResponseEntity<>(estados, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Error al obtener estados: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/estados/{id}")
    @Operation(
            summary = "Obtener estado por ID",
            description = "Retorna un estado específico según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadoDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Estado no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> obtenerporId(
            @Parameter(description = "ID del estado a obtener", example = "1")
            @PathVariable Integer id){
        try{
            EstadoDTO estado = estadoService.obtenerPorId(id);
            return new ResponseEntity<>(estado,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Error al obtener estado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

