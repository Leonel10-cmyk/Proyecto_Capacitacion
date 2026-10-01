package com.capacitacion.ebdn.controller;

import com.capacitacion.ebdn.dto.ApiResponse;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.service.ParticipanteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/participantes")
public class AdminParticipanteController {

    private final ParticipanteService participanteService;

    public AdminParticipanteController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Participante>>> listar(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "modalidad", required = false) String modalidad,
            @RequestParam(value = "estadoPago", required = false) String estadoPago,
            @RequestParam(value = "estadoRegistro", required = false) String estadoRegistro) {

        List<Participante> lista = participanteService.listar(query, modalidad, estadoPago, estadoRegistro);
        return ResponseEntity.ok(ApiResponse.ok("Participantes recuperados con éxito", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Participante>> obtenerPorId(@PathVariable Long id) {
        return participanteService.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Participante encontrado", p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Participante>> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El estado es requerido"));
        }

        try {
            Participante actualizado = participanteService.actualizarEstadoRegistro(id, nuevoEstado.trim());
            return ResponseEntity.ok(ApiResponse.ok("Estado actualizado exitosamente", actualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarExcel() {
        try {
            byte[] excelBytes = participanteService.exportarAExcel();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Participantes_Capacitacion_SQLServer.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
