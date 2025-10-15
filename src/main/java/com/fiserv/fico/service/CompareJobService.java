package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelId;
import com.fiserv.fico.domain.AluguelOrigem;
import com.fiserv.fico.repository.AluguelOrigemRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompareJobService {

  private final CompareService compareService;
  private final AluguelOrigemRepository origemRepository;
  private final JobRegistry registry = new JobRegistry();
  private final Executor executor = defaultExecutor();

  public UUID startJob() {
    UUID id = UUID.randomUUID();
    // Mark as running; file will be created immediately when the worker starts
    registry.put(id, JobStatus.RUNNING, null, null);
    CompletableFuture.runAsync(() -> runJob(id), executor);
    return id;
  }

  public JobInfo getJob(UUID id) {
    return registry.get(id);
  }

  public Resource getResultFile(UUID id) {
    JobInfo info = registry.get(id);
    if (info == null || info.getFilePath() == null) {
      return null;
    }
    return new FileSystemResource(info.getFilePath());
  }

  private void runJob(UUID id) {
    try {
      // Prepare output path and an initial empty workbook to allow partial download
      Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
      Path out = tempDir.resolve(id.toString() + ".xlsx");
      try (XSSFWorkbook init = new XSSFWorkbook()) {
        Files.newOutputStream(out).write(toBytes(init));
      }
      registry.put(id, JobStatus.RUNNING, out.toString(), null);

      List<AluguelOrigem> all = origemRepository.findAll();
      Map<String, List<AluguelOrigem>> byAlianca =
          all.stream().collect(Collectors.groupingBy(AluguelOrigem::getAlianca));

      // Deduplicate compare keys
      List<AluguelId> uniqueKeys =
          all.stream()
              .map(AluguelOrigem::getId)
              .collect(Collectors.collectingAndThen(
                  Collectors.toMap(
                      k -> k.getInstitutionNumber() + "|" + k.getServiceContract() + "|" + k.getAnomes(),
                      k -> k,
                      (a, b) -> a,
                      ConcurrentHashMap::new),
                  m -> new ArrayList<>(m.values())));

      LocalDateTime now = LocalDateTime.now();
      String hora = now.format(DateTimeFormatter.ISO_LOCAL_TIME);
      String data = now.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
      String mmaa = formatMMAA(now.toLocalDate());

      List<ConsolidatedInfo> consolidado = new ArrayList<>();
      for (AluguelId key : uniqueKeys) {
        var report =
            compareService.compare(
                key.getInstitutionNumber(), key.getServiceContract(), key.getAnomes());
        String status = report.divergent().isEmpty() ? "OK" : "Verificar";
        // Find an alianca representative for this key if available
        String alianca = all.stream()
            .filter(o -> {
              AluguelId i = o.getId();
              return i.getInstitutionNumber().equals(key.getInstitutionNumber())
                  && i.getServiceContract().equals(key.getServiceContract())
                  && i.getAnomes().equals(key.getAnomes());
            })
            .map(AluguelOrigem::getAlianca)
            .findFirst()
            .orElse("-");
        consolidado.add(
            ConsolidatedInfo.builder()
                .negocio(alianca)
                .institution_number(key.getInstitutionNumber())
                .service_contract(key.getServiceContract())
                .qtde_divergencias(report.divergent().size())
                .hora(hora)
                .data(data)
                .status(status)
                .build());
      }

      // Write XLSX (final or updated partial)
      try (XSSFWorkbook wb = new XSSFWorkbook()) {
        writeConsolidationSheet(wb, consolidado);
        // Per-alianca sheets with all AluguelOrigem columns
        for (Map.Entry<String, List<AluguelOrigem>> e : byAlianca.entrySet()) {
          writeAliancaSheet(wb, e.getKey(), e.getValue(), mmaa);
        }
        Files.newOutputStream(out).write(toBytes(wb));
      }
      registry.put(id, JobStatus.DONE, out.toString(), null);
    } catch (Exception ex) {
      log.error("Compare job failed", ex);
      registry.put(id, JobStatus.ERROR, null, ex.getMessage());
    }
  }

  private byte[] toBytes(XSSFWorkbook wb) throws IOException {
    try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
      wb.write(bos);
      return bos.toByteArray();
    }
  }

  private void writeConsolidationSheet(XSSFWorkbook wb, List<ConsolidatedInfo> list) {
    Sheet sheet = wb.createSheet("Consolidacao");
    int r = 0;
    Row header = sheet.createRow(r++);
    String[] cols = {
      "negocio", "institution_number", "service_contract", "qtde_divergencias", "hora", "data", "status"
    };
    for (int i = 0; i < cols.length; i++) {
      Cell c = header.createCell(i);
      c.setCellValue(cols[i]);
    }
    // Sort for stable output
    list.sort(Comparator.comparing(ConsolidatedInfo::getNegocio, Comparator.nullsLast(String::compareTo))
        .thenComparing(ConsolidatedInfo::getInstitution_number)
        .thenComparing(ConsolidatedInfo::getService_contract));
    for (ConsolidatedInfo ci : list) {
      Row row = sheet.createRow(r++);
      int c = 0;
      row.createCell(c++).setCellValue(nullToEmpty(ci.getNegocio()));
      row.createCell(c++).setCellValue(nullToEmpty(ci.getInstitution_number()));
      row.createCell(c++).setCellValue(nullToEmpty(ci.getService_contract()));
      row.createCell(c++).setCellValue(ci.getQtde_divergencias());
      row.createCell(c++).setCellValue(nullToEmpty(ci.getHora()));
      row.createCell(c++).setCellValue(nullToEmpty(ci.getData()));
      row.createCell(c++).setCellValue(nullToEmpty(ci.getStatus()));
    }
    for (int i = 0; i < cols.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void writeAliancaSheet(XSSFWorkbook wb, String alianca, List<AluguelOrigem> list, String mmaa) {
    String base = (alianca == null || alianca.isBlank()) ? "NA" : alianca;
    String name = sanitizeSheetName("Analítico_" + base + "_" + mmaa);
    // Sheet names limited to 31 chars
    if (name.length() > 31) name = name.substring(0, 31);
    Sheet sheet = wb.createSheet(name);
    int r = 0;
    Row header = sheet.createRow(r++);

    // Build header: id fields first, then other properties of AluguelOrigem
    List<String> headers = new ArrayList<>();
    headers.add("institutionNumber");
    headers.add("serviceContract");
    headers.add("anomes");
    headers.add("merchantNumber");
    headers.add("terminalId");

    // Discover other properties via JavaBeans Introspector (excluding id/class)
    try {
      var info = java.beans.Introspector.getBeanInfo(AluguelOrigem.class, Object.class);
      for (var pd : info.getPropertyDescriptors()) {
        String prop = pd.getName();
        if ("id".equals(prop)) continue;
        headers.add(prop);
      }
    } catch (java.beans.IntrospectionException ignored) {
      // fallback: at least include alianca if reflection fails
      if (!headers.contains("alianca")) headers.add("alianca");
    }

    for (int i = 0; i < headers.size(); i++) {
      header.createCell(i).setCellValue(headers.get(i));
    }

    for (AluguelOrigem o : list) {
      Row row = sheet.createRow(r++);
      int c = 0;
      AluguelId id = o.getId();
      row.createCell(c++).setCellValue(id != null ? nullToEmpty(id.getInstitutionNumber()) : "");
      row.createCell(c++).setCellValue(id != null ? nullToEmpty(id.getServiceContract()) : "");
      row.createCell(c++).setCellValue(id != null ? nullToEmpty(id.getAnomes()) : "");
      row.createCell(c++).setCellValue(id != null ? nullToEmpty(id.getMerchantNumber()) : "");
      row.createCell(c++).setCellValue(id != null ? nullToEmpty(id.getTerminalId()) : "");

      // Other properties by reflection following the same order as headers (skipping first 5)
      for (int i = 5; i < headers.size(); i++) {
        String prop = headers.get(i);
        String value = safeReadProperty(o, prop);
        row.createCell(c++).setCellValue(value);
      }
    }
    for (int i = 0; i < headers.size(); i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private String formatMMAA(LocalDate date) {
    String mm = String.format("%02d", date.getMonthValue());
    String aa = String.format("%02d", date.getYear() % 100);
    return mm + aa;
  }

  private String sanitizeSheetName(String name) {
    // Remove/replace invalid Excel sheet name characters and trim
    String sanitized = name.replace(':', '-')
        .replace('\\', '-')
        .replace('/', '-')
        .replace('?', '-')
        .replace('*', '-')
        .replace('[', '(')
        .replace(']', ')');
    // Excel also disallows leading/trailing apostrophes
    sanitized = sanitized.replaceAll("^'+|'+$", "");
    return sanitized;
  }

  private String safeReadProperty(Object bean, String property) {
    try {
      var info = java.beans.Introspector.getBeanInfo(bean.getClass(), Object.class);
      for (var pd : info.getPropertyDescriptors()) {
        if (pd.getName().equals(property) && pd.getReadMethod() != null) {
          Object val = pd.getReadMethod().invoke(bean);
          return val == null ? "" : String.valueOf(val);
        }
      }
    } catch (Exception ignored) {
    }
    return "";
  }

  private Executor defaultExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(2);
    ex.setMaxPoolSize(4);
    ex.setQueueCapacity(100);
    ex.setThreadNamePrefix("compare-job-");
    ex.initialize();
    return ex;
  }

  public enum JobStatus { RUNNING, DONE, ERROR }

  @Getter
  public static class JobInfo {
    private final JobStatus status;
    private final String filePath;
    private final String error;

    public JobInfo(JobStatus status, String filePath, String error) {
      this.status = status; this.filePath = filePath; this.error = error;
    }
  }

  private static class JobRegistry {
    private final ConcurrentHashMap<UUID, JobInfo> map = new ConcurrentHashMap<>();
    void put(UUID id, JobStatus status, String file, String error) {
      map.put(id, new JobInfo(status, file, error));
    }
    JobInfo get(UUID id) { return map.get(id); }
  }
}
