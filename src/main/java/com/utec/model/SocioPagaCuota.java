package com.utec.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "socio_paga_cuota")
public class SocioPagaCuota {
    @EmbeddedId
    private SocioPagaCuotaId id;

    @MapsId("idUsuario")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "FK_Socio_Paga"))
    private Socio socio;

    @MapsId("idCuota")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cuota", nullable = false,
            foreignKey = @ForeignKey(name = "FK_Cuota_Paga"))
    private Cuota cuota;

    @Column(name = "fech_cobro", nullable = false)
    @PastOrPresent(message = "La fecha de cobro no puede ser futura")
    private LocalDate fechCobro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_modo_pago", nullable = false,
            foreignKey = @ForeignKey(name = "FK_Modo_Pago_Cuota"))
    private ModoPago modoPago;

    @CreatedBy
    @Column(name = "Crea_Por")
    private String creaPor;

    @CreatedDate
    @Column(name = "Fech_Creacion")
    private LocalDate fechaCreacion;

    @LastModifiedBy
    @Column(name = "Modi_Por")
    private String modificadoPor;

    @LastModifiedDate
    @Column(name = "Fech_Modificacion")
    private LocalDate fechaModificacion;
}
