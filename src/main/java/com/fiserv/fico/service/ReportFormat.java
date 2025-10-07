package com.fiserv.fico.service;

public enum ReportFormat {
    JSON,
    CSV;

    public static ReportFormat from(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }
        try {
            return ReportFormat.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported report format: " + value, ex);
        }
    }
}
