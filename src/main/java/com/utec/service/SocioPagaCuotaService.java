package com.utec.service;

import com.utec.dto.ActividadDTO;
import com.utec.dto.SocioPagaCuotaDTO;
import com.utec.mapper.SocioPagaCuotaMapper;
import com.utec.model.*;
import com.utec.repository.CuotaRepository;
import com.utec.repository.ModoPagoRepository;
import com.utec.repository.SocioPagaCuotaRepository;
import com.utec.repository.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocioPagaCuotaService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private ModoPagoRepository modoPagoRepository;

    @Autowired
    private SocioPagaCuotaRepository socioPagaCuotaRepository;

    @Autowired
    private SocioPagaCuotaMapper socioPagaCuotaMapper;

    public void registrarPago(SocioPagaCuotaDTO dto) {

       if(dto.getFechaCobro().isBefore(LocalDate.of(1900,1,1))){
           throw new IllegalArgumentException("La fecha de pago no puede ser anterior a 1900 ");
       }
        // Verificar si ya existe un pago para este socio y esta cuota
        boolean yaExiste = socioPagaCuotaRepository.existsById(
                new SocioPagaCuotaId(dto.getIdUsuario(), dto.getIdCuota())
        );
        if (yaExiste) {
            throw new IllegalStateException("Ya existe un pago registrado para este socio y cuota.");
        }

           Socio socio = socioRepository.findById(dto.getIdUsuario())
                   .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        if (socio.getEstadoSocio().getIdestado() == null || socio.getEstadoSocio().getIdestado() != 1) {
            throw new IllegalStateException("El socio no esta activo.");
        }
           Cuota cuota = cuotaRepository.findById(dto.getIdCuota())
                   .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));
        if (cuota.getId() < socio.getPagaDesde() || cuota.getId() > socio.getPagaHasta()) {
            throw new IllegalStateException("La cuota no esta dentro de los meses permitidos para este socio.");
        }
           ModoPago modoPago = modoPagoRepository.findById(dto.getModoPago())
                   .orElseThrow(() -> new RuntimeException("Modo de pago no encontrado"));


           SocioPagaCuota pago = socioPagaCuotaMapper.toEntity(dto, socio, cuota, modoPago);

           try {
               socioPagaCuotaRepository.save(pago);
           } catch (Exception e) {
               Throwable root = e;
               while (root.getCause() != null) {
                   root = root.getCause();
               }
               throw new RuntimeException("Error al guardar el pago: "
                       + root.getClass().getSimpleName()
                       + " → " + root.getMessage(), e);
}
    }

    public void modificarPago(SocioPagaCuotaDTO dto) {
        if(dto.getFechaCobro().isBefore(LocalDate.of(1900,1,1))){
            throw new IllegalArgumentException("La fecha de pago no puede ser anterior a 1900 ");
        }else{

        SocioPagaCuotaId id = new SocioPagaCuotaId(dto.getIdUsuario(), dto.getIdCuota());

        SocioPagaCuota pagoExistente = socioPagaCuotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede modificar: el socio no ha pagado esa cuota."));

        Socio socio = socioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        if (socio.getEstadoSocio().getIdestado() == null || socio.getEstadoSocio().getIdestado() != 1) {
            throw new IllegalStateException("El socio no esta activo.");
        }
        // actualizar datos
        ModoPago nuevoModoPago = modoPagoRepository.findById(dto.getModoPago())
                .orElseThrow(() -> new RuntimeException("Modo de pago no encontrado"));

        pagoExistente.setFechCobro(dto.getFechaCobro());
        pagoExistente.setModoPago(nuevoModoPago);

            socioPagaCuotaRepository.save(pagoExistente);

        }

    }

    public List<SocioPagaCuotaDTO> ObtenerPagosCuotas(){
        List<SocioPagaCuota> lista = socioPagaCuotaRepository.findAll();
        return lista.stream()
                .map(socioPagaCuotaMapper::toDTO)
                .collect(Collectors.toList());
    }

}
