package com.capacitacion.ebdn.service;

import com.capacitacion.ebdn.dto.PagoRequest;
import com.capacitacion.ebdn.entity.Pago;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.repository.PagoRepository;
import com.capacitacion.ebdn.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ParticipanteRepository participanteRepository;
    private final FileStorageService fileStorageService;

    public PagoService(PagoRepository pagoRepository,
                       ParticipanteRepository participanteRepository,
                       FileStorageService fileStorageService) {
        this.pagoRepository = pagoRepository;
        this.participanteRepository = participanteRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Pago agregarPago(PagoRequest req, MultipartFile comprobante, String adminUsername) {
        if (req.getParticipanteId() == null) {
            throw new IllegalArgumentException("El ID del participante es obligatorio.");
        }

        Participante participante = participanteRepository.findById(req.getParticipanteId())
                .orElseThrow(() -> new IllegalArgumentException("Participante no encontrado con ID: " + req.getParticipanteId()));

        String opCode = req.getCodigoOperacion() != null ? req.getCodigoOperacion().trim() : "";
        if (!opCode.isEmpty() && pagoRepository.existsByCodigoOperacion(opCode)) {
            throw new IllegalArgumentException("El número de operación '" + opCode + "' ya está registrado en el sistema.");
        }

        int siguienteCuota = participante.getPagos().size() + 1;
        if (req.getNumeroCuota() != null && req.getNumeroCuota() > 0) {
            siguienteCuota = req.getNumeroCuota();
        }

        String comprobanteUrl = null;
        if (comprobante != null && !comprobante.isEmpty()) {
            comprobanteUrl = fileStorageService.storeFile(comprobante, "cuota" + siguienteCuota + "_" + participante.getDni());
        }

        Pago pago = new Pago();
        pago.setNumeroCuota(siguienteCuota);
        pago.setMedioPago(req.getMedioPago() != null ? req.getMedioPago().trim() : "Efectivo");
        pago.setMonto(req.getMonto() != null ? req.getMonto() : BigDecimal.ZERO);
        pago.setCodigoOperacion(opCode);
        pago.setComprobanteUrl(comprobanteUrl);
        pago.setEstadoValidacion("Validado");
        pago.setValidadoPor(adminUsername);
        pago.setFechaValidacion(LocalDateTime.now());

        participante.agregarPago(pago);
        participanteRepository.save(participante);

        return pago;
    }

    @Transactional
    public Pago cambiarEstadoValidacion(Long pagoId, String nuevoEstado, String adminUsername) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoId));

        pago.setEstadoValidacion(nuevoEstado);
        pago.setValidadoPor(adminUsername);
        pago.setFechaValidacion(LocalDateTime.now());

        pagoRepository.save(pago);

        // Actualizar estado general del participante si todos los pagos están validados
        Participante p = pago.getParticipante();
        if (p != null) {
            boolean allValidated = p.getPagos().stream()
                    .allMatch(pg -> "Validado".equalsIgnoreCase(pg.getEstadoValidacion()));
            if (allValidated && "Pago Completo".equalsIgnoreCase(p.getEstadoPago())) {
                p.setEstadoRegistro("Validado");
            }
            participanteRepository.save(p);
        }

        return pago;
    }

    @Transactional
    public Map<String, Object> procesarConciliacionMasivaYape(MultipartFile file, String adminUsername) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes subir un archivo CSV válido.");
        }

        List<String> opCodesFound = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Separar por comas o punto y coma
                String[] parts = line.split("[,;]");
                for (String part : parts) {
                    String clean = part.replaceAll("[^0-9]", "").trim();
                    if (clean.length() >= 6 && clean.length() <= 12) {
                        opCodesFound.add(clean);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el archivo CSV: " + e.getMessage());
        }

        int validados = 0;
        List<String> codigosValidados = new ArrayList<>();

        for (String op : opCodesFound) {
            Optional<Pago> pagoOpt = pagoRepository.findByCodigoOperacion(op);
            if (pagoOpt.isPresent()) {
                Pago pago = pagoOpt.get();
                if (!"Validado".equalsIgnoreCase(pago.getEstadoValidacion())) {
                    pago.setEstadoValidacion("Validado");
                    pago.setValidadoPor(adminUsername + " (Yape CSV)");
                    pago.setFechaValidacion(LocalDateTime.now());
                    pagoRepository.save(pago);
                    validados++;
                    codigosValidados.add(op);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCodigosEncontrados", opCodesFound.size());
        result.put("pagosValidados", validados);
        result.put("codigosValidados", codigosValidados);
        return result;
    }
}
