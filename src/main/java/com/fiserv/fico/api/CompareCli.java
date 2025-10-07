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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
public class CompareCli implements CommandLineRunner {

    private final CompareService service;
    private final ReportWriter reportWriter;

    public CompareCli(CompareService service, ReportWriter reportWriter) {
        this.service = service;
        this.reportWriter = reportWriter;
    }

    @Override
    public void run(String... args) {
        Map<String, String> params = parseArgs(args);
        if (!params.containsKey("institutionNumber")
                || !params.containsKey("serviceContract")
                || !params.containsKey("anomes")) {
            System.err.println(
                    "Missing required arguments. Usage: --institutionNumber=<value> "
                            + "--serviceContract=<value> --anomes=<value>");
            return;
        }

        ReportFormat format;
        try {
            format = ReportFormat.from(params.get("format"));
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        Optional<Path> outputPath = Optional.ofNullable(params.get("out")).map(Paths::get);

        CompareReport report = service.compare(
                params.get("institutionNumber"),
                params.get("serviceContract"),
                params.get("anomes"));

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
