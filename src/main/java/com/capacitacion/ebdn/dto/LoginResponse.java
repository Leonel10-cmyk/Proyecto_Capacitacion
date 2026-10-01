package com.capacitacion.ebdn.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String username;
    private String nombreCompleto;
    private String rol;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String token, String username, String nombreCompleto, String rol) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
