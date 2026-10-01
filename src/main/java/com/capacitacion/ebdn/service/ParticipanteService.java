package com.capacitacion.ebdn.service;

import com.capacitacion.ebdn.dto.RegistroRequest;
import com.capacitacion.ebdn.entity.Pago;
import com.capacitacion.ebdn.entity.Participante;
import com.capacitacion.ebdn.repository.PagoRepository;
import com.capacitacion.ebdn.repository.ParticipanteRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final PagoRepository pagoRepository;
    private final FileStorageService fileStorageService;

    public ParticipanteService(ParticipanteRepository participanteRepository,
                               PagoRepository pagoRepository,
                               FileStorageService fileStorageService) {
        this.participanteRepository = participanteRepository;
        this.pagoRepository = pagoRepository;
        this.fileStorageService = fileStorageService;
    }

    public BigDecimal calcularCostoModalidad(String modalidad) {
        if (modalidad == null) return new BigDecimal("20.00");
        String modLower = modalidad.trim().toLowerCase();

        switch (modLower) {
            case "presencial":
                return new BigDecimal("25.00");
            case "virtual":
                return new BigDecimal("20.00");
            case "solo manual":
                return new BigDecimal("35.00");
            case "presencial + manual":
            case "presencial + manual (físico)":
                return new BigDecimal("60.00");
            case "virtual + manual":
            case "virtual + manual (físico)":
                return new BigDecimal("55.00");
            default:
                return new BigDecimal("20.00");
        }
    }

    @Transactional
    public Participante registrarPublico(RegistroRequest req, MultipartFile comprobante) {
        String dni = req.getDni() != null ? req.getDni().trim() : "";
        if (dni.isEmpty()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        if (participanteRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("Ya existe un participante registrado con el DNI " + dni);
        }

        String opCode = req.getCodigoOperacion() != null ? req.getCodigoOperacion().trim() : "";
        if (!opCode.isEmpty() && pagoRepository.existsByCodigoOperacion(opCode)) {
            throw new IllegalArgumentException("El número de operación '" + opCode + "' ya fue registrado anteriormente.");
        }

        Participante p = new Participante();
        p.setNombre(req.getNombre() != null ? req.getNombre().trim() : "");
        p.setApellidos(req.getApellidos() != null ? req.getApellidos().trim() : "");
        p.setDni(dni);
        p.setCorreo(req.getCorreo() != null ? req.getCorreo().trim() : "");
        p.setTelefono(req.getTelefono() != null ? req.getTelefono().trim() : "");
        p.setIglesia(req.getIglesia() != null ? req.getIglesia().trim() : "");
        p.setCargo(req.getCargo() != null ? req.getCargo().trim() : "");
        p.setModalidad(req.getModalidad() != null ? req.getModalidad().trim() : "Virtual");

        BigDecimal costoTotal = calcularCostoModalidad(p.getModalidad());
        p.setCostoTotal(costoTotal);
        p.setEstadoRegistro("Pendiente");
        p.setTicketQr("QR-" + dni + "-" + System.currentTimeMillis());

        // Manejo del comprobante subido
        String comprobanteUrl = null;
        if (comprobante != null && !comprobante.isEmpty()) {
            comprobanteUrl = fileStorageService.storeFile(comprobante, "voucher_" + dni);
        }

        // Crear primer pago (Cuota 1)
        BigDecimal montoPago = req.getMontoPago() != null ? req.getMontoPago() : BigDecimal.ZERO;
        Pago cuota1 = new Pago();
        cuota1.setNumeroCuota(1);
        cuota1.setMedioPago(req.getMedioPago() != null ? req.getMedioPago().trim() : "Yape");
        cuota1.setMonto(montoPago);
        cuota1.setCodigoOperacion(opCode);
        cuota1.setComprobanteUrl(comprobanteUrl);
        cuota1.setEstadoValidacion("Pendiente");

        p.agregarPago(cuota1);

        return participanteRepository.save(p);
    }

    public List<Participante> listar(String query, String modalidad, String estadoPago, String estadoRegistro) {
        return participanteRepository.filtrarParticipantes(query, modalidad, estadoPago, estadoRegistro);
    }

    public Optional<Participante> buscarPorId(Long id) {
        return participanteRepository.findById(id);
    }

    public Optional<Participante> buscarPorDni(String dni) {
        return participanteRepository.findByDni(dni != null ? dni.trim() : "");
    }

    @Transactional
    public Participante actualizarEstadoRegistro(Long id, String nuevoEstado) {
        Participante p = participanteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participante no encontrado con ID: " + id));
        p.setEstadoRegistro(nuevoEstado);
        return participanteRepository.save(p);
    }

    @Transactional
    public Participante marcarAsistencia(String dni, int dia) {
        Participante p = participanteRepository.findByDni(dni != null ? dni.trim() : "")
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún participante con el DNI: " + dni));

        if (dia == 1) {
            p.setAsistenciaDia1(true);
        } else if (dia == 2) {
            p.setAsistenciaDia2(true);
        } else {
            throw new IllegalArgumentException("El día debe ser 1 o 2.");
        }
        return participanteRepository.save(p);
    }

    public byte[] exportarAExcel() throws IOException {
        List<Participante> lista = participanteRepository.findAllByOrderByFechaRegistroDesc();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Participantes Capacitación");

            // Estilos
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Cabeceras
            String[] headers = {
                "ID", "Nombres", "Apellidos", "DNI", "Correo", "Teléfono", "Iglesia", "Cargo",
                "Modalidad", "Costo Total (S/)", "Total Pagado (S/)", "Deuda (S/)", "Estado Pago",
                "Estado Registro", "Asistencia D1", "Asistencia D2", "Fecha Registro", "Nro Cuotas"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowIdx = 1;
            for (Participante p : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getApellidos());
                row.createCell(3).setCellValue(p.getDni());
                row.createCell(4).setCellValue(p.getCorreo() != null ? p.getCorreo() : "");
                row.createCell(5).setCellValue(p.getTelefono() != null ? p.getTelefono() : "");
                row.createCell(6).setCellValue(p.getIglesia() != null ? p.getIglesia() : "");
                row.createCell(7).setCellValue(p.getCargo() != null ? p.getCargo() : "");
                row.createCell(8).setCellValue(p.getModalidad());
                row.createCell(9).setCellValue(p.getCostoTotal() != null ? p.getCostoTotal().doubleValue() : 0.0);
                row.createCell(10).setCellValue(p.getTotalPagado() != null ? p.getTotalPagado().doubleValue() : 0.0);
                row.createCell(11).setCellValue(p.getDeuda() != null ? p.getDeuda().doubleValue() : 0.0);
                row.createCell(12).setCellValue(p.getEstadoPago());
                row.createCell(13).setCellValue(p.getEstadoRegistro());
                row.createCell(14).setCellValue(Boolean.TRUE.equals(p.getAsistenciaDia1()) ? "Sí" : "No");
                row.createCell(15).setCellValue(Boolean.TRUE.equals(p.getAsistenciaDia2()) ? "Sí" : "No");
                row.createCell(16).setCellValue(p.getFechaRegistro() != null ? p.getFechaRegistro().format(dtf) : "");
                row.createCell(17).setCellValue(p.getPagos() != null ? p.getPagos().size() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
