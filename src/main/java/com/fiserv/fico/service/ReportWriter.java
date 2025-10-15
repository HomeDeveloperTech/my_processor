package com.fiserv.fico.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportWriter {

  private static final String SECTION_ONLY_IN_ALIANCA = "ONLY_IN_ALIANCA";
  private static final String SECTION_ONLY_IN_EXCECAO = "ONLY_IN_EXCECAO";
  private static final String SECTION_DIVERGENT = "DIVERGENT";

  private final ObjectMapper objectMapper;

  public void write(CompareReport report, ReportFormat format, Optional<Path> outputPath) {
    String content = serialize(report, format);
    if (outputPath.isPresent()) {
      Path path = outputPath.get();
      try {
        Path parent = path.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to write report to file: " + path, e);
      }
    } else {
      System.out.println(content);
    }
  }

  String serialize(CompareReport report, ReportFormat format) {
    switch (format) {
      case CSV:
        return toCsv(report);
      case JSON:
        return toJson(report);
      default:
        throw new IllegalArgumentException("Unsupported format: " + format);
    }
  }

  private String toJson(CompareReport report) {
    try {
      return objectMapper.writeValueAsString(report);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize compare report", e);
    }
  }

  private String toCsv(CompareReport report) {
    String lineSeparator = System.lineSeparator();
    StringBuilder builder = new StringBuilder();

    appendOnlyInAliancaSection(builder, report.onlyInAlianca(), lineSeparator);
    builder.append(lineSeparator);
    appendOnlyInExcecaoSection(builder, report.onlyInExcecao(), lineSeparator);
    builder.append(lineSeparator);
    appendDivergentSection(builder, report.divergent(), lineSeparator);

    return builder.toString();
  }

  private void appendOnlyInAliancaSection(
      StringBuilder builder, List<AluguelProcessamentoAlianca> registros, String lineSeparator) {
    builder.append(SECTION_ONLY_IN_ALIANCA).append(lineSeparator);
    builder
        .append(
            "id,institutionNumber,serviceContract,anomes,merchantNumber,terminalId,valorCorrigido")
        .append(lineSeparator);
    if (registros == null) {
      return;
    }
    for (AluguelProcessamentoAlianca registro : registros) {
      builder
          .append(csvValue(registro.getId()))
          .append(',')
          .append(csvValue(registro.getInstitutionNumber()))
          .append(',')
          .append(csvValue(registro.getServiceContract()))
          .append(',')
          .append(csvValue(registro.getAnomes()))
          .append(',')
          .append(csvValue(registro.getMerchantNumber()))
          .append(',')
          .append(csvValue(registro.getTerminalId()))
          .append(',')
          .append(csvValue(registro.getValorCorrigido()))
          .append(lineSeparator);
    }
  }

  private void appendOnlyInExcecaoSection(
      StringBuilder builder, List<AluguelExcecao> registros, String lineSeparator) {
    builder.append(SECTION_ONLY_IN_EXCECAO).append(lineSeparator);
    builder
        .append("id,institutionNumber,serviceContract,anoMes,merchantNumber,dataValue,valRental")
        .append(lineSeparator);
    if (registros == null) {
      return;
    }
    for (AluguelExcecao registro : registros) {
      builder
          .append(csvValue(registro.getId()))
          .append(',')
          .append(csvValue(registro.getInstitutionNumber()))
          .append(',')
          .append(csvValue(registro.getServiceContract()))
          .append(',')
          .append(csvValue(registro.getAnomes()))
          .append(',')
          .append(csvValue(registro.getMerchantNumber()))
          .append(',')
          .append(csvValue(registro.getTerminalId()))
          .append(',')
          .append(csvValue(registro.getValRental()))
          .append(lineSeparator);
    }
  }

  private void appendDivergentSection(
      StringBuilder builder, List<DivergentRecord> registros, String lineSeparator) {
    builder.append(SECTION_DIVERGENT).append(lineSeparator);
    builder.append("institution,service,anomes,field,leftValue,rightValue").append(lineSeparator);
    if (registros == null) {
      return;
    }
    for (DivergentRecord registro : registros) {
      if (registro.diffs() == null || registro.diffs().isEmpty()) {
        builder
            .append(csvValue(registro.key().institution()))
            .append(',')
            .append(csvValue(registro.key().service()))
            .append(',')
            .append(csvValue(registro.key().anomes()))
            .append(',')
            .append(csvValue(null))
            .append(',')
            .append(csvValue(null))
            .append(',')
            .append(csvValue(null))
            .append(lineSeparator);
        continue;
      }
      for (ValueDiff diff : registro.diffs()) {
        builder
            .append(csvValue(registro.key().institution()))
            .append(',')
            .append(csvValue(registro.key().service()))
            .append(',')
            .append(csvValue(registro.key().anomes()))
            .append(',')
            .append(csvValue(diff.field()))
            .append(',')
            .append(csvValue(diff.leftValue()))
            .append(',')
            .append(csvValue(diff.rightValue()))
            .append(lineSeparator);
      }
    }
  }

  private String csvValue(Object value) {
    if (value == null) {
      return "";
    }
    String text;
    if (value instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal) value;
      text = bd.toPlainString();
    } else {
      text = value.toString();
    }
    boolean needsQuotes =
        text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
    if (text.contains("\"")) {
      text = text.replace("\"", "\"\"");
    }
    if (needsQuotes) {
      text = "\"" + text + "\"";
    }
    return text;
  }
}
