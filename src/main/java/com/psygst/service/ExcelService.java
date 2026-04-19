package com.psygst.service;

import com.psygst.model.Pago;
import com.psygst.repository.PagoRepository;
import com.psygst.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public byte[] generarReporteMensual(int year, int month) {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        LocalDate inicio = LocalDate.of(year, month, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Pago> pagos = pagoRepository.findByProfesionalAndPeriod(idProfesional, inicio, fin);

        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Reporte Mensual");

            // Styles
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font whiteFont = wb.createFont();
            whiteFont.setColor(IndexedColors.WHITE.getIndex());
            whiteFont.setBold(true);
            headerStyle.setFont(whiteFont);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String monthName = Month.of(month).name();
            titleCell.setCellValue("Reporte Mensual PsyGst — " + monthName + " " + year);
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Header row
            Row headerRow = sheet.createRow(2);
            String[] headers = { "Fecha", "Paciente", "DNI", "Modalidad", "Monto", "Pagado", "Método" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Data rows
            double totalSesiones = 0;
            double totalPagado = 0;
            int row = 3;
            for (Pago p : pagos) {
                Row dataRow = sheet.createRow(row++);
                dataRow.createCell(0).setCellValue(p.getTurno().getFecha().toString());
                dataRow.createCell(1).setCellValue(
                        p.getTurno().getPaciente().getNombre() + " " + p.getTurno().getPaciente().getApellido());
                dataRow.createCell(2).setCellValue(p.getTurno().getPaciente().getDni());
                dataRow.createCell(3).setCellValue(p.getTurno().getModalidad());
                dataRow.createCell(4).setCellValue(p.getMonto().doubleValue());
                dataRow.createCell(5).setCellValue(p.getPagado() ? "SÍ" : "NO");
                dataRow.createCell(6).setCellValue(p.getMetodoPago() != null ? p.getMetodoPago() : "-");
                totalSesiones += p.getMonto().doubleValue();
                if (p.getPagado())
                    totalPagado += p.getMonto().doubleValue();
            }

            // Summary rows
            row += 1;
            Row totalRow = sheet.createRow(row++);
            totalRow.createCell(3).setCellValue("TOTAL FACTURADO:");
            totalRow.createCell(4).setCellValue(totalSesiones);
            Row cobradoRow = sheet.createRow(row);
            cobradoRow.createCell(3).setCellValue("TOTAL COBRADO:");
            cobradoRow.createCell(4).setCellValue(totalPagado);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando reporte Excel", e);
        }
    }
}
