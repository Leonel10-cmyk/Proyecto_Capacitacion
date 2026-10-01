package com.capacitacion.ebdn.controller;

import com.capacitacion.ebdn.dto.ApiResponse;
import com.capacitacion.ebdn.dto.RegistroRequest;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.service.ParticipanteService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ParticipanteService participanteService;

    public PublicController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrar(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("dni") String dni,
            @RequestParam(value = "correo", required = false) String correo,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "iglesia", required = false) String iglesia,
            @RequestParam(value = "cargo", required = false) String cargo,
            @RequestParam(value = "modalidad", defaultValue = "Virtual") String modalidad,
            @RequestParam(value = "montoPago", defaultValue = "0") BigDecimal montoPago,
            @RequestParam(value = "medioPago", defaultValue = "Yape") String medioPago,
            @RequestParam(value = "codigoOperacion", required = false) String codigoOperacion,
            @RequestParam(value = "comprobante", required = false) MultipartFile comprobante) {

        try {
            RegistroRequest req = new RegistroRequest();
            req.setNombre(nombre);
            req.setApellidos(apellidos);
            req.setDni(dni);
            req.setCorreo(correo);
            req.setTelefono(telefono);
            req.setIglesia(iglesia);
            req.setCargo(cargo);
            req.setModalidad(modalidad);
            req.setMontoPago(montoPago);
            req.setMedioPago(medioPago);
            req.setCodigoOperacion(codigoOperacion);

            Participante guardado = participanteService.registrarPublico(req, comprobante);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", guardado.getId());
            data.put("nombreCompleto", guardado.getNombre() + " " + guardado.getApellidos());
            data.put("dni", guardado.getDni());
            data.put("costoTotal", guardado.getCostoTotal());
            data.put("totalPagado", guardado.getTotalPagado());
            data.put("deuda", guardado.getDeuda());
            data.put("estadoPago", guardado.getEstadoPago());
            data.put("ticketQr", guardado.getTicketQr());

            return ResponseEntity.ok(ApiResponse.ok("¡Inscripción realizada con éxito!", data));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error al registrar: " + ex.getMessage()));
        }
    }

    @GetMapping("/modalidades")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> listarModalidades() {
        Map<String, BigDecimal> precios = new LinkedHashMap<>();
        precios.put("Presencial", new BigDecimal("25.00"));
        precios.put("Virtual", new BigDecimal("20.00"));
        precios.put("Solo manual", new BigDecimal("35.00"));
        precios.put("Presencial + manual", new BigDecimal("60.00"));
        precios.put("Virtual + manual", new BigDecimal("55.00"));
        return ResponseEntity.ok(ApiResponse.ok("Modalidades obtenidas con éxito", precios));
    }
}
