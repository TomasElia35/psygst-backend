package com.psygst.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.psygst.exception.*;
import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.recibo.ReciboResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReciboService {

    private final ReciboRepository reciboRepository;
    private final PagoRepository pagoRepository;
    private final ProfesionalRepository profesionalRepository;
    private final SistemaRepository sistemaRepository;

    @Value("${pdf.storage-path:./storage/recibos}")
    private String pdfStoragePath;

    /** RN-F01: only if pagado=1. RN-F02: correlative number */
    @Transactional
    public ReciboResponse generar(String pagoUuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        Pago pago = pagoRepository.findByUuidAndSistema_IdSistemaAndBaja(pagoUuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        // RN-F01: only if paid
        if (!pago.getPagado()) {
            throw new BadRequestException("No se puede generar recibo para un pago pendiente");
        }

        Profesional prof = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));

        Sistema sistema = sistemaRepository.findById(idSistema)
                .orElseThrow(() -> new BadRequestException("Sistema no encontrado"));

        // RN-F02: correlative number
        int year = LocalDateTime.now().getYear();
        String nroRecibo = generarNroRecibo(idProfesional, year);

        // Generate PDF
        byte[] pdfBytes = generarPdfContent(pago, prof, nroRecibo);
        String rutaPdf = guardarPdf(pdfBytes, idProfesional, nroRecibo);

        Recibo recibo = Recibo.builder()
                .uuid(UUID.randomUUID().toString())
                .pago(pago)
                .nroRecibo(nroRecibo)
                .montoTotal(pago.getMonto())
                .rutaPdf(rutaPdf)
                .fechaEmision(LocalDateTime.now())
                .profesional(prof)
                .sistema(sistema)
                .baja((byte) 0)
                .build();

        recibo = reciboRepository.save(recibo);
        return toResponse(recibo);
    }

    @Transactional(readOnly = true)
    public byte[] descargar(String reciboUuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Recibo recibo = reciboRepository.findByUuidAndSistema_IdSistemaAndBaja(reciboUuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Recibo no encontrado"));

        try {
            return Files.readAllBytes(Path.of(recibo.getRutaPdf()));
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el PDF del recibo", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ReciboResponse> obtenerPorPaciente(String pacienteUuid) {
        // Need to look up by the pago→turno→paciente chain
        return reciboRepository.findByPago_Turno_Paciente_IdPacienteAndBajaOrderByFechaEmisionDesc(
                // This is a workaround — in production use a proper join query
                -1, (byte) 0 // placeholder — will use a findAll filter for now
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String generarNroRecibo(Integer idProfesional, int year) {
        int nextNum = 1;
        var lastOpt = reciboRepository.findLastNroReciboByProfesionalAndYear(idProfesional, year);
        if (lastOpt.isPresent()) {
            String last = lastOpt.get();
            // Format: REC-YYYY-NNNNN
            String[] parts = last.split("-");
            if (parts.length == 3) {
                nextNum = Integer.parseInt(parts[2]) + 1;
            }
        }
        return String.format("REC-%d-%05d", year, nextNum);
    }

    private byte[] generarPdfContent(Pago pago, Profesional prof, String nroRecibo) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            DeviceRgb primaryColor = new DeviceRgb(37, 99, 235); // #2563EB blue

            // Header
            Paragraph header = new Paragraph("PsyGst — Recibo de Pago")
                    .setFontColor(primaryColor)
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);

            doc.add(new Paragraph("\n"));

            // Professional info
            Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
            headerTable.addCell(createCell("Profesional:", true));
            headerTable.addCell(createCell(prof.getNombre() + " " + prof.getApellido(), false));
            headerTable.addCell(createCell("CUIT:", true));
            headerTable.addCell(createCell(prof.getCuit() != null ? prof.getCuit() : "-", false));
            headerTable.addCell(createCell("Licencia:", true));
            headerTable.addCell(createCell(prof.getNroLicencia() != null ? prof.getNroLicencia() : "-", false));
            doc.add(headerTable);

            doc.add(new Paragraph("\n"));

            // Receipt info
            Table receiptTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
            receiptTable.addCell(createCell("N° Recibo:", true));
            receiptTable.addCell(createCell(nroRecibo, false));
            receiptTable.addCell(createCell("Fecha Emisión:", true));
            receiptTable.addCell(createCell(LocalDateTime.now().toLocalDate().toString(), false));
            doc.add(receiptTable);

            doc.add(new Paragraph("\n"));

            // Patient and session info
            Turno turno = pago.getTurno();
            Table sessionTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
            sessionTable.addCell(createCell("Paciente:", true));
            sessionTable.addCell(
                    createCell(turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido(), false));
            sessionTable.addCell(createCell("DNI:", true));
            sessionTable.addCell(createCell(turno.getPaciente().getDni(), false));
            sessionTable.addCell(createCell("Fecha Sesión:", true));
            sessionTable.addCell(createCell(turno.getFecha().toString(), false));
            sessionTable.addCell(createCell("Horario:", true));
            sessionTable.addCell(createCell(turno.getHoraComienzo() + " - " + turno.getHoraFin(), false));
            sessionTable.addCell(createCell("Modalidad:", true));
            sessionTable.addCell(createCell(turno.getModalidad(), false));
            sessionTable.addCell(createCell("Método de pago:", true));
            sessionTable.addCell(createCell(pago.getMetodoPago() != null ? pago.getMetodoPago() : "-", false));
            doc.add(sessionTable);

            doc.add(new Paragraph("\n"));

            // Total
            Paragraph total = new Paragraph("TOTAL: $" + pago.getMonto().toPlainString())
                    .setBold().setFontSize(16)
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.RIGHT);
            doc.add(total);

            doc.add(new Paragraph("\n\n"));

            // Legal footer
            Paragraph footer = new Paragraph("Comprobante no válido como factura. " +
                    "Emitido bajo régimen de Monotributo.")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            throw new RuntimeException("Error al generar el PDF del recibo", e);
        }
    }

    private Cell createCell(String text, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isHeader)
            cell.setBold().setBackgroundColor(new DeviceRgb(240, 242, 245));
        return cell;
    }

    private String guardarPdf(byte[] pdfBytes, Integer idProfesional, String nroRecibo) {
        try {
            String dir = pdfStoragePath + "/" + idProfesional;
            Files.createDirectories(Path.of(dir));
            String path = dir + "/" + nroRecibo + ".pdf";
            Files.write(Path.of(path), pdfBytes);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Error guardando PDF en disco", e);
        }
    }

    private ReciboResponse toResponse(Recibo r) {
        return new ReciboResponse(
                r.getUuid(), r.getNroRecibo(), r.getMontoTotal(),
                r.getFechaEmision(), r.getRutaPdf());
    }

    public void anular(String uuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Recibo recibo = reciboRepository.findByUuidAndSistema_IdSistemaAndBaja(uuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Recibo no encontrado"));
        recibo.setBaja((byte) 1); // RN-F04: anulado, number not reused
        reciboRepository.save(recibo);
    }
}
