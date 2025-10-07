package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareReport;
import com.fiserv.fico.service.CompareService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
public class CompareCli implements CommandLineRunner {

    private final CompareService service;
    private final ObjectMapper objectMapper;

    public CompareCli(CompareService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
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

        CompareReport report = service.compare(
                params.get("institutionNumber"),
                params.get("serviceContract"),
                params.get("anomes"));

        try {
            System.out.println(objectMapper.writeValueAsString(report));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize compare report", e);
        }
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
