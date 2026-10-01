package com.capacitacion.ebdn.controller;

import com.capacitacion.ebdn.dto.ApiResponse;
import com.capacitacion.ebdn.dto.PagoRequest;
import com.capacitacion.ebdn.entity.Pago;
import com.capacitacion.ebdn.service.PagoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pagos")
public class AdminPagoController {

    private final PagoService pagoService;

    public AdminPagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping(value = "/cuota", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Pago>> agregarCuota(
            @RequestParam("participanteId") Long participanteId,
            @RequestParam("monto") BigDecimal monto,
            @RequestParam(value = "numeroCuota", required = false) Integer numeroCuota,
            @RequestParam(value = "medioPago", defaultValue = "Efectivo") String medioPago,
            @RequestParam(value = "codigoOperacion", required = false) String codigoOperacion,
            @RequestParam(value = "comprobante", required = false) MultipartFile comprobante,
            Authentication authentication) {

        try {
            PagoRequest req = new PagoRequest();
            req.setParticipanteId(participanteId);
            req.setMonto(monto);
            req.setNumeroCuota(numeroCuota);
            req.setMedioPago(medioPago);
            req.setCodigoOperacion(codigoOperacion);

            String adminUser = authentication != null ? authentication.getName() : "admin";
            Pago pago = pagoService.agregarPago(req, comprobante, adminUser);

            return ResponseEntity.ok(ApiResponse.ok("Pago registrado exitosamente", pago));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error al registrar pago: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<ApiResponse<Pago>> validarPago(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String nuevoEstado = body.getOrDefault("estado", "Validado");
        String adminUser = authentication != null ? authentication.getName() : "admin";

        try {
            Pago pago = pagoService.cambiarEstadoValidacion(id, nuevoEstado, adminUser);
            return ResponseEntity.ok(ApiResponse.ok("Estado de pago actualizado", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/conciliacion-yape", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> conciliacionYape(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String adminUser = authentication != null ? authentication.getName() : "admin";
            Map<String, Object> resultado = pagoService.procesarConciliacionMasivaYape(file, adminUser);
            return ResponseEntity.ok(ApiResponse.ok("Conciliación Yape procesada con éxito", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error al procesar archivo Yape: " + e.getMessage()));
        }
    }
}
