package com.capacitacion.ebdn.service;

import com.capacitacion.ebdn.dto.LoginRequest;
import com.capacitacion.ebdn.dto.LoginResponse;
import com.capacitacion.ebdn.entity.Usuario;
import com.capacitacion.ebdn.repository.UsuarioRepository;
import com.capacitacion.ebdn.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername().trim(),
                        loginRequest.getPassword().trim()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername().trim())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticar"));

        return new LoginResponse(
                true,
                "Inicio de sesión exitoso",
                jwt,
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol()
        );
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario registrarAdminSiNoExiste(String username, String rawPassword, String nombreCompleto, String correo) {
        return usuarioRepository.findByUsername(username).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setNombreCompleto(nombreCompleto);
            u.setCorreo(correo);
            u.setRol("ROLE_ADMIN");
            u.setActivo(true);
            return usuarioRepository.save(u);
        });
    }
}
