package com.utec.service;

import com.utec.dto.*;
import com.utec.enums.TipoReporteEnum;
import com.utec.mapper.UsuarioConcurreActividadMapper;
import com.utec.model.*;
import com.utec.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioConcurreActividadService {

    @Autowired
    private UsuarioConcurreActividadMapper usuarioConcurreActividadMapper;
    @Autowired
    private UsuarioConcurreActividadRepository usuarioConcurreActividadRepository;
    @Autowired
    private ActividadRepository actividadRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    //para los pagos
    @Autowired
    private UsuarioConcurreActividadMapper mapper;

    @Autowired
    private ModoPagoRepository modoPagoRepository;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioConcurreActividadService.class);

    public UsuarioConcurreActividadService(UsuarioConcurreActividadMapper usuarioConcurreActividadMapper
            ,UsuarioRepository usuarioRepository
            ,ActividadRepository actividadRepository
            ,UsuarioConcurreActividadRepository usuarioConcurreActividadRepository){
        this.usuarioConcurreActividadMapper = usuarioConcurreActividadMapper;
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
        this.usuarioConcurreActividadRepository = usuarioConcurreActividadRepository;
    }

    public void inscribirUsuario(Integer id_usuario, Integer id_actividad, UsuarioConcurreActividadDTO dto){
        if(usuarioConcurreActividadRepository.existsByUsuarioIdAndActividadIdAndEstadoIdestado(id_usuario, id_actividad, 1)){
            throw new IllegalArgumentException("El usuario ya está inscrito en esta actividad.");
        }

        System.out.println("Buscando usuario y actividad...");
        Usuario usuario = usuarioRepository.findById(id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Actividad actividad = actividadRepository.findById(id_actividad)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

        //Validar que la actividad requiere inscripción
        if (!actividad.isRequ_inscripcion()) {
            throw new IllegalArgumentException("La actividad no requiere inscripción.");
        }


        //Validar que esté en período de inscripción
        LocalDate hoy = LocalDate.now();
        if (actividad.getFech_apertura_inscripcion() != null &&
                actividad.getFech_apertura_inscripcion().isAfter(hoy)) {
            throw new IllegalArgumentException("La inscripción aún no está habilitada para esta actividad.");
        }
        if (actividad.getFech_hora_actividad().toLocalDate().isBefore(hoy)) {
            throw new IllegalArgumentException("No se puede inscribir a una actividad ya realizada.");
        }


        UsuarioConcurreActividad entidad = usuarioConcurreActividadMapper.toEntity(dto, usuario, actividad);
        Estado estadoActivo = estadoRepository.findById(1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado activo no encontrado"));
        entidad.setEstado(estadoActivo);

       validarCuposActividad(actividad);
        usuarioConcurreActividadRepository.save(entidad);

        logger.info("Inscribiendo a usuario a la actividad: {}",actividad.getNombre());
        logger.info("Detalle de usuario: {}", usuario.getCorreo());
        logger.debug("Detalles: {}",dto);
    }

    public void cancelarInscripcion(Integer id_usuario,Integer id_actividad, boolean activar){
        UsuarioConcurreActividad inscripcion = usuarioConcurreActividadRepository
                .findByUsuarioIdAndActividadId(id_usuario,id_actividad)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrado"));

        Integer idEstado = activar ? 1 : 2;
        logger.info("Cancelando inscripcion a la actividad: {}",inscripcion.getActividad().getNombre());
        logger.info("Detalle de usuario: {}",inscripcion.getUsuario().getCorreo());

        if(!activar && inscripcion.getEstado().getIdestado() == 2){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La inscripcion ya esta inactiva");
        }
        if (activar && inscripcion.getEstado().getIdestado() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La inscripción ya esta activa.");
        }
        Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado"));

        inscripcion.setEstado(estado);
        usuarioConcurreActividadRepository.save(inscripcion);

    }

    public ReporteTipoActDTO obtenerReportePorTipoActividad(FiltroReportePorTipoActividadDTO filtroDTO) {
        LocalDate fechaDesde = filtroDTO.getFechaDesde().toLocalDate();
        LocalDate fechaHasta = filtroDTO.getFechaHasta().toLocalDate();
        List<String> tiposActividad = filtroDTO.getTiposActividad();


        if (tiposActividad != null && tiposActividad.isEmpty()) {
            tiposActividad = null;
        }

        List<ReportePorTipoActividadDTO> datos = usuarioConcurreActividadRepository
                .obtenerReportePorTipoActividad(fechaDesde, fechaHasta, tiposActividad);

        int totalInscriptos = datos.stream()
                .mapToInt(ReportePorTipoActividadDTO::getCantidadInscriptos)
                .sum();

        int totalCancelados = datos.stream()
                .mapToInt(ReportePorTipoActividadDTO::getCantidadCancelados)
                .sum();

        return new ReporteTipoActDTO(totalInscriptos, totalCancelados, datos);
    }


    /**
     * Genera un reporte detallado de inscripciones y cancelaciones según el requerimiento RF 005-08
     * @param filtro Filtros para el reporte (fechas, tipo de reporte, actividades)
     * @return Reporte detallado con totales y desglose por actividad
     */
    public ReporteInscripcionDetalladoDTO generarReporteDetallado(FiltroReporteInscripcionDTO filtro) {
        logger.info("Generando reporte detallado de inscripciones con filtros: {}", filtro);
        
        // Validar fechas
        if (filtro.getFechaDesde() == null || filtro.getFechaHasta() == null) {
            throw new IllegalArgumentException("Las fechas desde y hasta son obligatorias");
        }
        
        if (filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        
        // Convertir LocalDateTime a LocalDate para las consultas
        LocalDate fechaDesde = filtro.getFechaDesde().toLocalDate();
        LocalDate fechaHasta = filtro.getFechaHasta().toLocalDate();
        
        // Obtener reporte detallado por actividad
        List<ReporteInscripcionPorActividadDTO> reportePorActividad =
            usuarioConcurreActividadRepository.obtenerReporteDetalladoPorFechas(
                fechaDesde, 
                fechaHasta, 
                filtro.getIdActividades()
            );
        
        System.out.println(reportePorActividad);
        
        ReporteInscripcionDTO totales = usuarioConcurreActividadRepository.obtenerTotalesGenerales(
            fechaDesde, 
            fechaHasta, 
            filtro.getIdActividades()
        );
        System.out.println(totales);
        
        if (TipoReporteEnum.INSCRIPCION.equals(filtro.getTipoReporte())) {
            reportePorActividad = reportePorActividad.stream()
                .filter(r -> r.getCantidadInscripciones() > 0)
                .toList();
        } else if (TipoReporteEnum.CANCELACION.equals(filtro.getTipoReporte())) {
            reportePorActividad = reportePorActividad.stream()
                .filter(r -> r.getCantidadCancelaciones() > 0)
                .toList();
        }
        
        return ReporteInscripcionDetalladoDTO.builder()
            .totalInscripciones(totales.getCantidadInscriptos())
            .totalCancelaciones(totales.getCantidadCancelados())
            .reportePorActividad(reportePorActividad)
            .build();
    }
    
    /**
     * Obtiene la lista de IDs de actividades que tienen inscripciones registradas
     * @return Lista de IDs de actividades disponibles para filtros
     */
    public List<Integer> obtenerActividadesDisponibles() {
        logger.info("Obteniendo lista de actividades disponibles para filtros de reporte");
        return usuarioConcurreActividadRepository.obtenerActividadesDisponibles();
    }

    //MANEJO DE LOS PAGOS

    //obtener todos los registros
    public List<UsuarioConcurreActividadDTO> obtenerTodos() {
        return usuarioConcurreActividadRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    //para la actualizacion de los campos

    @Transactional
    public UsuarioConcurreActividadDTO actualizarPagoActividad(UsuarioConcurreActividadDTO dto) {
        // Crear la clave compuesta
        UsuarioConcurreActividadId id = new UsuarioConcurreActividadId(dto.getIdUsuario(), dto.getIdActividad());

        // Buscar el registro existente
        UsuarioConcurreActividad entidad = usuarioConcurreActividadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la relación usuario-actividad"));

        // Actualizar los campos de pago
        entidad.setFechCobro(dto.getFechCobro());
        entidad.setMontCobrado(dto.getMontCobrado());
        entidad.setPagoTicket(dto.getPagoTicket());
        entidad.setAsistencia(dto.getAsistencia());

        // Guardar y retornar
        usuarioConcurreActividadRepository.save(entidad);
        return mapper.toDto(entidad);
    }

    /*//nueva version para sumar montos
    @Transactional
    public UsuarioConcurreActividadDTO actualizarPagoActividad(UsuarioConcurreActividadDTO dto) {
        // Crear la clave compuesta
        UsuarioConcurreActividadId id = new UsuarioConcurreActividadId(dto.getIdUsuario(), dto.getIdActividad());

        // Buscar el registro existente
        UsuarioConcurreActividad entidad = usuarioConcurreActividadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la relación usuario-actividad"));

        // Acumular el pago en vez de sobrescribirlo
        BigDecimal montoActual = entidad.getMontCobrado() != null ? entidad.getMontCobrado() : BigDecimal.ZERO;
        BigDecimal nuevoMonto = dto.getMontCobrado() != null ? dto.getMontCobrado() : BigDecimal.ZERO;
        entidad.setMontCobrado(montoActual.add(nuevoMonto));

        // Actualizar otros campos si corresponde
        entidad.setFechCobro(dto.getFechCobro());
        entidad.setPagoTicket(dto.getPagoTicket());
        entidad.setAsistencia(dto.getAsistencia());

        // Guardar y retornar
        usuarioConcurreActividadRepository.save(entidad);
        return mapper.toDto(entidad);
    }
*/

    public void validarCuposActividad(Actividad actividad) {
        Integer capacidadMaxima = actividad.getEspacio().getCapacidadMaxima();

        int inscripcionesActivas = usuarioConcurreActividadRepository
                .countByActividadIdAndEstadoIdestado(actividad.getId(), 1); // 1 = estado activo

        System.out.println("DEBUG CUPOS:");
        System.out.println("Actividad ID: " + actividad.getId());
        System.out.println("Inscripciones activas: " + inscripcionesActivas);
        System.out.println("Capacidad máxima: " + capacidadMaxima);

        if (inscripcionesActivas >= capacidadMaxima) {
            throw new IllegalArgumentException("No hay cupos disponibles en el espacio asignado para esta actividad.");
        }
    }
}
