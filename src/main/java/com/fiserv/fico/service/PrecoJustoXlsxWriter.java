package com.fiserv.fico.service;

import com.fiserv.fico.domain.PrecoJustoSicredi;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class PrecoJustoXlsxWriter {

    void writeWorkbook(String anomes, String service,
                       List<PrecoJustoSicredi> registros,
                       Path outFile) throws IOException {
        if (outFile.getParent() != null) {
            Files.createDirectories(outFile.getParent());
        }
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            writeVisaoPorEcSheet(wb, registros);
            writeClientesNaoEnquadradosSheet(wb);
            writeAluguelPorTecnologiaTerminalSheet(wb);
            writeMdrPorBandeiraProdutoSheet(wb);
            writeAntecipacaoSheet(wb);

            try (java.io.OutputStream os = Files.newOutputStream(outFile)) {
                wb.write(os);
            }
        }
    }

    private void writeVisaoPorEcSheet(XSSFWorkbook wb, List<PrecoJustoSicredi> registros) {
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Não Enquadrados"));
        // Headers according to guideline mapping from PRECO_JUSTO_SICREDI
        String[] headers = new String[]{
                "CNPJ",                 // A - from PRECO_JUSTO_SICREDI
                "ANOMES",               // B - from PRECO_JUSTO_SICREDI
                "SERVICE_CONTRACT",     // C - from PRECO_JUSTO_SICREDI
                "FATURAMENTO_MEDIO_REALIZADO", // D - from PRECO_JUSTO_SICREDI
                "ORIGEM_MERCHANT",      // E - from *_ORIGEM_MERCHANT (placeholder)
                "FATURAMENTO_COMBINADO",         // F
                "FATURAMENTO_COMBINADO_MIN",     // G
                "FATURAMENTO_COMBINADO_MAX",     // H
                "FATURAMENTO_REALIZADO_M1",      // I
                "FATURAMENTO_REALIZADO_M2",      // J
                "FATURAMENTO_REALIZADO_M3"       // K
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(headers[i]);
        }
        if (registros == null) return;
        int rowIdx = 1;
        for (PrecoJustoSicredi r : registros) {
            Row row = sheet.createRow(rowIdx++);
            int col = 0;
            row.createCell(col++).setCellValue(nvl(r.getCnpj()));
            row.createCell(col++).setCellValue(nvl(r.getAnomes()));
            row.createCell(col++).setCellValue(nvl(r.getServiceContract()));
            setNumeric(row.createCell(col++), r.getFaturamentoMedioRealizado());
            row.createCell(col++).setCellValue(""); // ORIGEM_MERCHANT placeholder
            setNumeric(row.createCell(col++), r.getFaturamentoCombinado());
            setNumeric(row.createCell(col++), r.getFaturamentoCombinadoMin());
            setNumeric(row.createCell(col++), r.getFaturamentoCombinadoMax());
            setNumeric(row.createCell(col++), r.getFaturamentoRealizadoM1());
            setNumeric(row.createCell(col++), r.getFaturamentoRealizadoM2());
            setNumeric(row.createCell(col++), r.getFaturamentoRealizadoM3());
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeClientesNaoEnquadradosSheet(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Clientes elegiveis nao enquadrados"));
        String[] headers = new String[]{
                "CNPJ","ANOMES","SERVICE_CONTRACT","TAXA_ATUAL","TAXA_MAJORADA_ALVO","OBS"
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }
    }

    private void writeAluguelPorTecnologiaTerminalSheet(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Aluguel por tecnologia/terminal"));
        String[] headers = new String[]{
                "CNPJ","ANOMES","SERVICE_CONTRACT","TECNOLOGIA","TERMINAL_ID","TAXA_ATUAL","TAXA_MAJORADA"
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }
    }

    private void writeMdrPorBandeiraProdutoSheet(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("MDR por bandeira e produto"));
        String[] headers = new String[]{
                "CNPJ","ANOMES","SERVICE_CONTRACT","BANDEIRA","PRODUTO","TAXA_ATUAL","TAXA_MAJORADA"
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }
    }

    private void writeAntecipacaoSheet(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Antecipacao (auto/eventual)"));
        String[] headers = new String[]{
                "CNPJ","ANOMES","SERVICE_CONTRACT","TIPO_ANTECIPACAO","TAXA_ATUAL","TAXA_MAJORADA"
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i);
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private void setNumeric(Cell c, java.math.BigDecimal v) {
        if (v == null) {
            c.setBlank();
        } else {
            c.setCellValue(v.doubleValue());
        }
    }
}
