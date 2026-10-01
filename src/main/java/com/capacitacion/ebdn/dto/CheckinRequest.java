package com.capacitacion.ebdn.dto;

public class CheckinRequest {
    private String dni;
    private Integer dia; // 1 or 2

    public CheckinRequest() {}
    public CheckinRequest(String dni, Integer dia) {
        this.dni = dni;
        this.dia = dia;
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public Integer getDia() { return dia; }
    public void setDia(Integer dia) { this.dia = dia; }
}
