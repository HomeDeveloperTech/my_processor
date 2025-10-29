package com.fiserv.fico.service;

import com.fiserv.fico.domain.PrecoJustoSicredi;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrecoJustoXlsxWriterTest {

    @Test
    void writeWorkbook_shouldCreateAllSheetsAndHeaders_andPopulateRows() throws IOException {
        // arrange
        PrecoJustoXlsxWriter writer = new PrecoJustoXlsxWriter();
        PrecoJustoSicredi r1 = PrecoJustoSicredi.builder()
                .id(1L)
                .cnpj("12345678000199")
                .anomes("202401")
                .serviceContract("S1")
                .faturamentoMedioRealizado(new BigDecimal("100.10"))
                .faturamentoCombinado(new BigDecimal("200.20"))
                .faturamentoCombinadoMin(new BigDecimal("150.15"))
                .faturamentoCombinadoMax(new BigDecimal("250.25"))
                .faturamentoRealizadoM1(new BigDecimal("90.01"))
                .faturamentoRealizadoM2(new BigDecimal("91.02"))
                .faturamentoRealizadoM3(new BigDecimal("92.03"))
                .build();
        PrecoJustoSicredi r2 = PrecoJustoSicredi.builder()
                .id(2L)
                .cnpj(null) // ensure null becomes empty string
                .anomes("202402")
                .serviceContract("S2")
                .faturamentoMedioRealizado(null)
                .build();

        Path temp = Files.createTempFile("preco-justo-test", ".xlsx");
        try {
            // act
            writer.writeWorkbook("202401", "S1", List.of(r1, r2), temp);

            // assert by opening workbook
            try (XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(temp))) {
                // sheets present (names may be sanitized by POI, so use createSafeSheetName)
                assertThat(wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("Não Enquadrados"))).isNotNull();
                assertThat(wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("Clientes elegiveis nao enquadrados"))).isNotNull();
                assertThat(wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("Aluguel por tecnologia/terminal"))).isNotNull();
                assertThat(wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("MDR por bandeira e produto"))).isNotNull();
                assertThat(wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("Antecipacao (auto/eventual)"))).isNotNull();

                // verify headers of first sheet
                Sheet s = wb.getSheet(org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName("Não Enquadrados"));
                Row header = s.getRow(0);
                assertThat(header.getCell(0).getStringCellValue()).isEqualTo("CNPJ");
                assertThat(header.getCell(1).getStringCellValue()).isEqualTo("ANOMES");
                assertThat(header.getCell(2).getStringCellValue()).isEqualTo("SERVICE_CONTRACT");
                assertThat(header.getCell(3).getStringCellValue()).isEqualTo("FATURAMENTO_MEDIO_REALIZADO");
                assertThat(header.getCell(4).getStringCellValue()).isEqualTo("ORIGEM_MERCHANT");

                // verify first data row values (some numerics)
                Row row1 = s.getRow(1);
                assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("12345678000199");
                assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("202401");
                assertThat(row1.getCell(2).getStringCellValue()).isEqualTo("S1");
                assertThat(row1.getCell(3).getNumericCellValue()).isEqualTo(100.10d);
                assertThat(row1.getCell(5).getNumericCellValue()).isEqualTo(200.20d);
                assertThat(row1.getCell(6).getNumericCellValue()).isEqualTo(150.15d);
                assertThat(row1.getCell(7).getNumericCellValue()).isEqualTo(250.25d);
                assertThat(row1.getCell(8).getNumericCellValue()).isEqualTo(90.01d);
                assertThat(row1.getCell(9).getNumericCellValue()).isEqualTo(91.02d);
                assertThat(row1.getCell(10).getNumericCellValue()).isEqualTo(92.03d);

                // verify second row converts nulls appropriately
                Row row2 = s.getRow(2);
                assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("");
                assertThat(row2.getCell(3).getCellType()).as("null numeric should be blank").hasToString("BLANK");
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
