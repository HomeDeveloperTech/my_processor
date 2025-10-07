package com.fiserv.fico.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReportWriter reportWriter = new ReportWriter(objectMapper);

    @Test
    void shouldSerializeReportAsJson() throws IOException {
        CompareReport report = sampleReport();

        String serialized = reportWriter.serialize(report, ReportFormat.JSON);

        JsonNode result = objectMapper.readTree(serialized);
        JsonNode expected = objectMapper.readTree(objectMapper.writeValueAsString(report));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldSerializeReportAsCsv() {
        CompareReport report = sampleReport();

        String serialized = reportWriter.serialize(report, ReportFormat.CSV);

        String lineSeparator = System.lineSeparator();
        String expected = new StringBuilder()
                .append("ONLY_IN_ALIANCA").append(lineSeparator)
                .append("id,institutionNumber,serviceContract,anomes,merchantNumber,terminalId,valorCorrigido")
                .append(lineSeparator)
                .append("1,001,ABC,202401,123,321,10.50").append(lineSeparator)
                .append(lineSeparator)
                .append("ONLY_IN_EXCECAO").append(lineSeparator)
                .append("id,institutionNumber,serviceContract,anoMes,merchantNumber,dataValue,valRental")
                .append(lineSeparator)
                .append("2,001,ABC,202401,123,2024-01,11.75").append(lineSeparator)
                .append(lineSeparator)
                .append("DIVERGENT").append(lineSeparator)
                .append("institution,service,anomes,field,leftValue,rightValue").append(lineSeparator)
                .append("001,ABC,202401,valorCorrigido,10.50,11.75").append(lineSeparator)
                .toString();

        assertThat(serialized).isEqualTo(expected);
    }

    @Test
    void shouldWriteReportToFile(@TempDir Path tempDir) throws IOException {
        CompareReport report = sampleReport();
        Path output = tempDir.resolve("report.csv");

        reportWriter.write(report, ReportFormat.CSV, Optional.of(output));

        String content = Files.readString(output);
        assertThat(content).isEqualTo(reportWriter.serialize(report, ReportFormat.CSV));
    }

    private CompareReport sampleReport() {
        AluguelProcessamentoAlianca alianca = new AluguelProcessamentoAlianca();
        alianca.setId(1L);
        alianca.setInstitutionNumber("001");
        alianca.setServiceContract("ABC");
        alianca.setAnomes("202401");
        alianca.setMerchantNumber("123");
        alianca.setTerminalId("321");
        alianca.setValorCorrigido(new BigDecimal("10.50"));

        AluguelExcecao excecao = new AluguelExcecao();
        excecao.setId(2L);
        excecao.setInstitutionNumber("001");
        excecao.setServiceContract("ABC");
        excecao.setAnoMes("202401");
        excecao.setMerchantNumber("123");
        excecao.setDataValue("2024-01");
        excecao.setValRental(new BigDecimal("11.75"));

        DivergentRecord divergent = new DivergentRecord(
                new CompareKey("001", "ABC", "202401"),
                List.of(new ValueDiff("valorCorrigido", "10.50", "11.75")));

        return new CompareReport(List.of(alianca), List.of(excecao), List.of(divergent));
    }
}
