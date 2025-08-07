package com.utec.service;

import com.utec.dto.ActividadDTO;
import com.utec.dto.GetActividadDTO;
import com.utec.mapper.ActividadMapper;
import com.utec.model.Actividad;
import com.utec.model.Espacio;
import com.utec.model.Estado;
import com.utec.model.TipoActividad;
import com.utec.model.Usuario;
import com.utec.model.ModoPago;
import com.utec.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActividadService {

    @Autowired
    private ActividadMapper actividadMapper;
    @Autowired
    private ActividadRepository actividadRepository;
    @Autowired
    private TipoActividadRepository tipoActividadRepository;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private ModoPagoRepository modoPagoRepository;
    @Autowired
    private EstadoRepository estadoRepository;


    private static final Logger logger = LoggerFactory.getLogger(ActividadService.class);

    public ActividadService(ActividadRepository actividadRepository, ActividadMapper actividadMapper, TipoActividadRepository tipoActividadRepository, EspacioRepository espacioRepository, ModoPagoRepository modoPagoRepository, EstadoRepository estadoRepository, UsuarioRepository usuarioRepository) {
        this.actividadRepository = actividadRepository;
        this.actividadMapper = actividadMapper;
        this.tipoActividadRepository = tipoActividadRepository;
        this.espacioRepository = espacioRepository;
        this.modoPagoRepository = modoPagoRepository;
        this.estadoRepository = estadoRepository;
    }

    public ActividadDTO crearActividad(ActividadDTO dto) throws NotFoundException {
        try {
        TipoActividad tipoActividad = this.tipoActividadRepository.findById(dto.getIdTipoActividad())
        .orElseThrow(() -> new NotFoundException());
      Espacio espacio = this.espacioRepository.findById(dto.getIdEspacio())
        .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado"));
        ModoPago modoPago = this.modoPagoRepository.findById(dto.getIdModoPago())
        .orElseThrow(() -> new NotFoundException());
        Estado estadoInactivo = this.estadoRepository.findById(2) //Estado inactivo
        .orElseThrow(() -> new NotFoundException());


        validarFechas(dto);
        validarInscripcion(dto);


        // validar que espacio esté disponible para fecha y duración
        LocalDateTime fechaInicio = dto.getFech_hora_actividad();
        LocalDateTime fechaFin = fechaInicio.plusHours(dto.getDuracion());


        List<Espacio> disponibles = espacioRepository.buscarEspaciosDisponiblesSinCapacidad(fechaInicio, fechaFin);


        boolean espacioDisponible = disponibles.stream()
                .anyMatch(e -> e.getIdEspacio().equals(espacio.getIdEspacio()));

        if (!espacioDisponible) {
            throw new IllegalStateException("El espacio no está disponible para la fecha y hora indicadas");
        }


        Actividad actividad = actividadMapper.toEntity(dto);
        actividad.setTipoActividad(tipoActividad);
        actividad.setEspacio(espacio);
        actividad.setModoPago(modoPago);
        actividad.setEstado(estadoInactivo);

        
        Actividad guardar = actividadRepository.save(actividad);
        logger.info("Creando actividad: {}",dto.getNombre());
        logger.debug("Detalles: {}",dto);

        return actividadMapper.toDto(guardar);
    } catch (Exception e) {
        System.out.println(e);
        throw e;
    }
    }

    public List<ActividadDTO> ObtenerActividades(){
        List<Actividad> actividades = actividadRepository.findAll();
        return actividades.stream()
                .map(actividadMapper::toDto)
                .collect(Collectors.toList());
    }

    public ActividadDTO obtenerPorId(Integer id)  {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrado con ID: " + id));
        return actividadMapper.toDto(actividad);
    }

    public List<GetActividadDTO> listarActividadesParaInscribirse() {
        List<Actividad> actividades = actividadRepository.findActividadesActivasParaInscripcionWithRelations();
        System.out.println(actividades);
        return actividades.stream()
                .map(actividadMapper::toGetActividadDTO)
                .collect(Collectors.toList());
    }

    public ActividadDTO actualizarActividad(ActividadDTO dto) {
        try {
            Actividad actividad = actividadRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("No se encuentra actividad con id " + dto.getId()));

            // Validar que la actividad no haya comenzado o finalizado
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime fechaFinActividad = actividad.getFech_hora_actividad().plusHours(actividad.getDuracion());

            if (actividad.getFech_hora_actividad().isBefore(ahora) || fechaFinActividad.isBefore(ahora)) {
                throw new IllegalStateException("No se puede modificar una actividad que ya comenzó o finalizó");
            }

            // Si requiere inscripción, validar que no se haya abierto la inscripción
            if (actividad.isRequ_inscripcion() && actividad.getFech_apertura_inscripcion() != null) {
                if (actividad.getFech_apertura_inscripcion().isBefore(ahora.toLocalDate()) ||
                        actividad.getFech_apertura_inscripcion().isEqual(ahora.toLocalDate())) {
                    throw new IllegalStateException("No se pueden realizar modificaciones desde la fecha de apertura de inscripción");
                }
            }

            // Solo obtener entidades relacionadas si vienen en el DTO
            TipoActividad tipoActividad = null;
            if (dto.getIdTipoActividad() != null) {
                tipoActividad = tipoActividadRepository.findById(dto.getIdTipoActividad())
                        .orElseThrow(() -> new EntityNotFoundException("TipoActividad no encontrado"));
            }

            Estado estado = null;
            if (dto.getIdEstado() != null) {
                estado = estadoRepository.findById(dto.getIdEstado())
                        .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado"));
            }

            Espacio espacio = null;
            if (dto.getIdEspacio() != null) {
                espacio = espacioRepository.findById(dto.getIdEspacio())
                        .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado"));
            }

            ModoPago modoPago = null;
            if (dto.getIdModoPago() != null) {
                modoPago = modoPagoRepository.findById(dto.getIdModoPago())
                        .orElseThrow(() -> new EntityNotFoundException("ModoPago no encontrado"));
            }

            // Validar fechas del DTO (mismas validaciones que en crear)
            validarFechas(dto);
            validarInscripcion(dto);

        if (dto.getIdEspacio() != null && !actividad.getEspacio().getIdEspacio().equals(dto.getIdEspacio()) ||
                dto.getFech_hora_actividad() != null && !actividad.getFech_hora_actividad().equals(dto.getFech_hora_actividad()) ||
                dto.getDuracion() != null && !actividad.getDuracion().equals(dto.getDuracion())) {

           LocalDateTime fechaInicio = dto.getFech_hora_actividad();
            LocalDateTime fechaFin = fechaInicio.plusHours(dto.getDuracion());


            List<Espacio> disponibles = espacioRepository.buscarEspaciosDisponiblesSinCapacidad(fechaInicio, fechaFin);

            boolean espacioDisponible = disponibles.stream()
                    .anyMatch(e -> e.getIdEspacio().equals(dto.getIdEspacio()));

            if (!espacioDisponible) {
                throw new IllegalStateException("El espacio no está disponible para la fecha y hora indicadas");
            }
        }

//id?
        if (dto.getDescripcion() != null) actividad.setDescripcion(dto.getDescripcion());
        // Actualizar campos (excepto nombre que no se puede modificar )
     if (dto.getObjetivo() !=null) actividad.setObjetivo(dto.getObjetivo());
     if (dto.isRequ_inscripcion() != actividad.isRequ_inscripcion()) actividad.setRequ_inscripcion(dto.isRequ_inscripcion());
     if (dto.getFech_apertura_inscripcion() !=null) actividad.setFech_apertura_inscripcion(dto.getFech_apertura_inscripcion());
     if (dto.getCosto() !=null) actividad.setCosto(dto.getCosto());
     if (dto.getObservaciones() !=null) actividad.setObservaciones(dto.getObservaciones());
     if (dto.getIdTipoActividad() !=null) actividad.setTipoActividad(tipoActividad);
     if (dto.getIdEstado() != null) actividad.setEstado(estado);
     if (dto.getIdEspacio() !=null) actividad.setEspacio(espacio);
     if (dto.getIdModoPago() !=null) actividad.setModoPago(modoPago);

    Actividad actualizado = actividadRepository.save(actividad);
    ActividadDTO dtoActualizado = actividadMapper.toDto(actualizado);

    logger.info("Actualizando actividad: {}", actividad.getNombre());
    logger.debug("Detalles: {}", dtoActualizado);

    return dtoActualizado;

        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }


    public void eliminarActividad(Integer id, boolean activar) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));

        Integer idEstadoObjetivo  = activar ? 1 : 2;


       if (!activar && actividad.getEstado().getIdestado() == 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La actividad ya está inactiva.");
        }

        if (activar && actividad.getEstado().getIdestado() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La actividad ya esta activa.");
        }


        if (!activar) {
            LocalDate hoy = LocalDate.now();
            LocalDateTime ahora = LocalDateTime.now();

            boolean puedeDarseDeBaja =
                    (actividad.isRequ_inscripcion() && actividad.getFech_apertura_inscripcion().isAfter(hoy)) || // aun no empezo inscripcion
                            actividad.getFech_hora_actividad().isBefore(ahora); // ya paso la actividad

            if (!puedeDarseDeBaja) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La actividad no puede darse de baja. Ya inicio la inscripcion.");
            }
        }
        Estado estadoInactivo = estadoRepository.findById(idEstadoObjetivo)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado " + (activar ? "activo" : "inactivo") + " no encontrado"));
        actividad.setEstado(estadoInactivo);
        actividadRepository.save(actividad);

       // logger.info("Dando baja lógica a la actividad: {}",actividad.getNombre());
        logger.info((activar ? "Reactivando" : "Dando baja lógica a") + " la actividad: {}", actividad.getNombre());
    }

    private void validarFechas(ActividadDTO dto) throws IllegalArgumentException {
        LocalDateTime ahora = LocalDateTime.now();

        // La actividad no puede ser en el pasado
        if (dto.getFech_hora_actividad().isBefore(ahora)) {
            throw new IllegalArgumentException("La fecha de la actividad no puede ser en el pasado");
        }

        // Si requiere inscripción, validar fecha de apertura
        if (dto.isRequ_inscripcion()) {
            if (dto.getFech_apertura_inscripcion() == null) {
                throw new IllegalArgumentException("Si requiere inscripcion, debe especificar fecha de apertura");
            }

            if (dto.getFech_apertura_inscripcion().isAfter(dto.getFech_hora_actividad().toLocalDate())) {
                throw new IllegalArgumentException("La fecha de apertura de inscripcion debe ser anterior a la actividad");
            }

            if (dto.getFech_apertura_inscripcion().isBefore(ahora.toLocalDate())) {
                throw new IllegalArgumentException("La fecha de apertura de inscripcion no puede ser en el pasado");
            }
        }
    }


    private void validarInscripcion(ActividadDTO dto) throws IllegalArgumentException {
        // Si no requiere inscripción, no debe tener fecha de apertura
        if (!dto.isRequ_inscripcion()  && dto.getFech_apertura_inscripcion() != null) {
            throw new IllegalArgumentException(
                    "No se puede establecer fecha de apertura si la actividad no requiere inscripción");
        }
    }



}