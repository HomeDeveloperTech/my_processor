package com.fiserv.fico.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
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

    String ls = System.lineSeparator();
    String[] lines = serialized.split("\r?\n");

    // Validate ONLY_IN_ALIANCA section
    assertThat(lines[0]).isEqualTo("ONLY_IN_ALIANCA");
    assertThat(lines[1])
        .isEqualTo("id,institutionNumber,serviceContract,anomes,merchantNumber,terminalId,valorCorrigido");
    assertThat(lines[2])
        .endsWith(",001,ABC,202401,123,321,10.50"); // ignore id value

    // Blank line between sections
    assertThat(lines[3]).isEmpty();

    // Validate ONLY_IN_EXCECAO section
    assertThat(lines[4]).isEqualTo("ONLY_IN_EXCECAO");
    assertThat(lines[5])
        .isEqualTo("id,institutionNumber,serviceContract,anoMes,merchantNumber,dataValue,valRental");
    assertThat(lines[6])
        .endsWith(",001,ABC,202401,123,2024-01,11.75"); // ignore id value

    // Blank line between sections
    assertThat(lines[7]).isEmpty();

    // Validate DIVERGENT section
    assertThat(lines[8]).isEqualTo("DIVERGENT");
    assertThat(lines[9]).isEqualTo("institution,service,anomes,field,leftValue,rightValue");
    assertThat(lines[10]).isEqualTo("001,ABC,202401,valorCorrigido,10.50,11.75");
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
    alianca.setInstitutionNumber("001");
    alianca.setServiceContract("ABC");
    alianca.setAnomes("202401");
    alianca.setMerchantNumber("123");
    alianca.setTerminalId("321");
    alianca.setValorCorrigido(new BigDecimal("10.50"));

    AluguelExcecao excecao = new AluguelExcecao();
    excecao.setInstitutionNumber("001");
    excecao.setServiceContract("ABC");
    excecao.setAnomes("202401");
    excecao.setMerchantNumber("123");
    excecao.setTerminalId("2024-01");
    excecao.setValRental(new BigDecimal("11.75"));

    DivergentRecord divergent =
        new DivergentRecord(
            new CompareKey("001", "ABC", "202401"),
            List.of(new ValueDiff("valorCorrigido", "10.50", "11.75")));

    return new CompareReport(List.of(alianca), List.of(excecao), List.of(divergent));
  }
}
