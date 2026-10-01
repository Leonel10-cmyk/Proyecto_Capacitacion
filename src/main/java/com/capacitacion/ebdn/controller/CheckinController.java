package com.capacitacion.ebdn.controller;

import com.capacitacion.ebdn.dto.ApiResponse;
import com.capacitacion.ebdn.dto.CheckinRequest;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.service.ParticipanteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/checkin")
public class CheckinController {

    private final ParticipanteService participanteService;

    public CheckinController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping("/buscar/{dni}")
    public ResponseEntity<ApiResponse<Participante>> buscarPorDni(@PathVariable String dni) {
        return participanteService.buscarPorDni(dni)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Participante encontrado", p)))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("No se encontró ningún participante con el DNI: " + dni)));
    }

    @PostMapping("/marcar")
    public ResponseEntity<ApiResponse<Participante>> marcarAsistencia(@RequestBody CheckinRequest request) {
        try {
            int dia = (request.getDia() != null && request.getDia() > 0) ? request.getDia() : 1;
            Participante p = participanteService.marcarAsistencia(request.getDni(), dia);
            return ResponseEntity.ok(ApiResponse.ok("¡Asistencia del Día " + dia + " marcada exitosamente!", p));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error al registrar asistencia: " + e.getMessage()));
        }
    }
}
