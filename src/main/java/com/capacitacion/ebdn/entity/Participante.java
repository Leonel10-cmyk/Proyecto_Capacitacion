package com.capacitacion.ebdn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participantes")
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(length = 120)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String iglesia;

    @Column(length = 100)
    private String cargo;

    @Column(nullable = false, length = 50)
    private String modalidad;

    @Column(name = "costo_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(name = "total_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPagado = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deuda = BigDecimal.ZERO;

    @Column(name = "estado_pago", length = 50)
    private String estadoPago = "Pendiente"; // Pago Completo, Pago Parcial, Pendiente

    @Column(name = "estado_registro", length = 50)
    private String estadoRegistro = "Pendiente"; // Pendiente, Validado, Rechazado

    @Column(name = "asistencia_dia1")
    private Boolean asistenciaDia1 = false;

    @Column(name = "asistencia_dia2")
    private Boolean asistenciaDia2 = false;

    @Column(name = "ticket_qr", length = 255)
    private String ticketQr;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_registro")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "participante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numeroCuota ASC")
    @JsonIgnoreProperties("participante")
    private List<Pago> pagos = new ArrayList<>();

    public Participante() {}

    // Helper methods to calculate totals
    public void recalcularTotales() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Pago p : pagos) {
            if (p.getMonto() != null) {
                sum = sum.add(p.getMonto());
            }
        }
        this.totalPagado = sum;
        if (this.costoTotal != null) {
            this.deuda = this.costoTotal.subtract(this.totalPagado);
            if (this.deuda.compareTo(BigDecimal.ZERO) <= 0) {
                this.deuda = BigDecimal.ZERO;
                this.estadoPago = "Pago Completo";
            } else if (this.totalPagado.compareTo(BigDecimal.ZERO) > 0) {
                this.estadoPago = "Falta Pagar S/ " + this.deuda.setScale(2);
            } else {
                this.estadoPago = "Pendiente";
            }
        }
    }

    public void agregarPago(Pago pago) {
        pagos.add(pago);
        pago.setParticipante(this);
        recalcularTotales();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getIglesia() { return iglesia; }
    public void setIglesia(String iglesia) { this.iglesia = iglesia; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public BigDecimal getTotalPagado() { return totalPagado; }
    public void setTotalPagado(BigDecimal totalPagado) { this.totalPagado = totalPagado; }

    public BigDecimal getDeuda() { return deuda; }
    public void setDeuda(BigDecimal deuda) { this.deuda = deuda; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(String estadoRegistro) { this.estadoRegistro = estadoRegistro; }

    public Boolean getAsistenciaDia1() { return asistenciaDia1; }
    public void setAsistenciaDia1(Boolean asistenciaDia1) { this.asistenciaDia1 = asistenciaDia1; }

    public Boolean getAsistenciaDia2() { return asistenciaDia2; }
    public void setAsistenciaDia2(Boolean asistenciaDia2) { this.asistenciaDia2 = asistenciaDia2; }

    public String getTicketQr() { return ticketQr; }
    public void setTicketQr(String ticketQr) { this.ticketQr = ticketQr; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
}
