package com.capacitacion.ebdn.controller;

import com.capacitacion.ebdn.dto.ApiResponse;
import com.capacitacion.ebdn.dto.LoginRequest;
import com.capacitacion.ebdn.dto.LoginResponse;
import com.capacitacion.ebdn.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = usuarioService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new LoginResponse(false, "Credenciales inválidas: " + e.getMessage(), null, null, null, null)
            );
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<String>> check(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.ok("Autenticado", authentication.getName()));
        }
        return ResponseEntity.ok(ApiResponse.error("No autenticado"));
    }
}
