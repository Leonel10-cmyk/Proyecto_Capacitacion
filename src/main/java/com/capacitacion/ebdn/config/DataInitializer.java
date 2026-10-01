package com.capacitacion.ebdn.config;

import com.capacitacion.ebdn.entity.Pago;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.repository.ParticipanteRepository;
import com.capacitacion.ebdn.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final ParticipanteRepository participanteRepository;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador Principal}")
    private String adminName;

    @Value("${app.admin.email:admin@capacitacion.pe}")
    private String adminEmail;

    public DataInitializer(UsuarioService usuarioService,
                           ParticipanteRepository participanteRepository) {
        this.usuarioService = usuarioService;
        this.participanteRepository = participanteRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Crear o sincronizar usuario administrador según variables de entorno
        usuarioService.registrarAdminSiNoExiste(
                adminUsername,
                adminPassword,
                adminName,
                adminEmail
        );
        System.out.println("✅ Usuario administrador verificado en base de datos: " + adminUsername);

        // 2. Si la tabla de participantes está vacía, insertar un par de registros de demostración
        if (participanteRepository.count() == 0) {
            Participante p1 = new Participante();
            p1.setNombre("Carlos");
            p1.setApellidos("Mendoza Huamán");
            p1.setDni("71234567");
            p1.setCorreo("carlos.mendoza@gmail.com");
            p1.setTelefono("987654321");
            p1.setIglesia("IEP Canto Grande");
            p1.setCargo("Maestro EBDN");
            p1.setModalidad("Presencial + manual");
            p1.setCostoTotal(new BigDecimal("60.00"));
            p1.setTicketQr("QR-71234567-001");
            p1.setEstadoRegistro("Validado");

            Pago pago1 = new Pago();
            pago1.setNumeroCuota(1);
            pago1.setMedioPago("Yape");
            pago1.setMonto(new BigDecimal("60.00"));
            pago1.setCodigoOperacion("12345678");
            pago1.setEstadoValidacion("Validado");
            pago1.setValidadoPor("admin");
            p1.agregarPago(pago1);

            participanteRepository.save(p1);

            Participante p2 = new Participante();
            p2.setNombre("María Elena");
            p2.setApellidos("Ramos Paredes");
            p2.setDni("45891234");
            p2.setCorreo("maria.ramos@gmail.com");
            p2.setTelefono("954321987");
            p2.setIglesia("Comunidad Cristiana Central");
            p2.setCargo("Directora Escuela Dominical");
            p2.setModalidad("Presencial");
            p2.setCostoTotal(new BigDecimal("25.00"));
            p2.setTicketQr("QR-45891234-002");
            p2.setEstadoRegistro("Pendiente");

            Pago pago2 = new Pago();
            pago2.setNumeroCuota(1);
            pago2.setMedioPago("Yape");
            pago2.setMonto(new BigDecimal("15.00"));
            pago2.setCodigoOperacion("87654321");
            pago2.setEstadoValidacion("Pendiente");
            p2.agregarPago(pago2);

            participanteRepository.save(p2);

            System.out.println("✅ Registros de prueba iniciales creados en SQL Server para Participantes.");
        }
    }
}
