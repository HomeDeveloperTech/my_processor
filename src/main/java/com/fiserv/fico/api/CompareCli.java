package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareReport;
import com.fiserv.fico.service.CompareService;
import com.fiserv.fico.service.ReportFormat;
import com.fiserv.fico.service.ReportWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
@Slf4j
@RequiredArgsConstructor
public class CompareCli implements CommandLineRunner {

  private final CompareService service;
  private final ReportWriter reportWriter;

  @Override
  public void run(String... args) {
    Map<String, String> params = parseArgs(args);
    if (!params.containsKey("institutionNumber")
        || !params.containsKey("serviceContract")
        || !params.containsKey("anomes")) {
      log.error(
          "Missing required arguments. Usage: --institutionNumber=<value> "
              + "--serviceContract=<value> --anomes=<value>");
      return;
    }

    ReportFormat format;
    try {
      format = ReportFormat.from(params.get("format"));
    } catch (IllegalArgumentException ex) {
      log.error(ex.getMessage());
      return;
    }

    Optional<Path> outputPath = Optional.ofNullable(params.get("out")).map(Paths::get);

    CompareReport report =
        service.compare(
            params.get("institutionNumber"), params.get("serviceContract"), params.get("anomes"));

    reportWriter.write(report, format, outputPath);
  }

  private Map<String, String> parseArgs(String[] args) {
    Map<String, String> params = new HashMap<>();
    for (String arg : args) {
      if (!arg.startsWith("--")) {
        continue;
      }
      int equalsIndex = arg.indexOf('=');
      if (equalsIndex < 0 || equalsIndex == arg.length() - 1) {
        continue;
      }
      String key = arg.substring(2, equalsIndex);
      String value = arg.substring(equalsIndex + 1);
      params.put(key, value);
    }
    return params;
  }
}
