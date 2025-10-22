package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelId;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

/**
 * IPP job to orchestrate Aluguel Postado processing and generate XLSX worksheets
 * following the guideline and leveraging consolidation patterns from CompareJobService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IppJobService {

  private final IppAluguelPostadoService ippService;
  private final AluguelProcessamentoAliancaRepository aliancaRepo;
  private final JobRegistry registry = new JobRegistry();
  private final Executor executor = defaultExecutor();

  public UUID startJob() {
    UUID id = UUID.randomUUID();
    registry.put(id, JobStatus.RUNNING, null, null);
    CompletableFuture.runAsync(() -> runJob(id), executor);
    return id;
  }

  public JobInfo getJob(UUID id) {
    return registry.get(id);
  }

  public Resource getResultFile(UUID id) {
    JobInfo info = registry.get(id);
    if (info == null || info.getFilePath() == null) return null;
    return new FileSystemResource(info.getFilePath());
  }

  private void runJob(UUID id) {
    try {
      Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
      Path out = tempDir.resolve(id.toString() + ".xlsx");
      // create initial empty workbook for partial download
      try (XSSFWorkbook init = new XSSFWorkbook()) {
        Files.newOutputStream(out).write(toBytes(init));
      }
      registry.put(id, JobStatus.RUNNING, out.toString(), null);

      // metadata and periods
      LocalDateTime now = LocalDateTime.now();
      String hora = now.format(DateTimeFormatter.ISO_LOCAL_TIME);
      String data = now.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
      String mmaa = String.format("%02d%02d", now.getMonthValue(), now.getYear() % 100);
      String anomesM = String.format(Locale.ROOT, "%04d%02d", now.getYear(), now.getMonthValue());
      LocalDate prev = now.toLocalDate().minusMonths(1);
      String anomesM1 = String.format(Locale.ROOT, "%04d%02d", prev.getYear(), prev.getMonthValue());

      // trigger processing (logs/report triggers handled inside the service)
      ippService.runForCurrentMonth();

      // Build report content
      List<String> contracts = List.of("110","149","125","104");
      Map<String, String> nameBySc = new HashMap<>();
      nameBySc.put("110", "SICREDI");
      nameBySc.put("149", "CAIXA");
      nameBySc.put("125", "BIN");
      nameBySc.put("104", "SICOOB_SIPAG");

      // Baseline ranges (mirror IppAluguelPostadoService)
      Map<String, Range> baseline = Map.of(
          "110", new Range(BigDecimal.ZERO, new BigDecimal("500000")),
          "149", new Range(BigDecimal.ZERO, new BigDecimal("500000")),
          "125", new Range(BigDecimal.ZERO, new BigDecimal("300000")),
          "104", new Range(new BigDecimal("-50000"), BigDecimal.ZERO)
      );

      // Write workbook
      try (XSSFWorkbook wb = new XSSFWorkbook()) {
        // Consolidated sheet
        Sheet cons = wb.createSheet("Consolidado_" + mmaa);
        int r = 0;
        Row header = cons.createRow(r++);
        String[] cols = {"negocio","service_contract","M","totalM","M-1","totalM1","delta","status","hora","data"};
        for (int i = 0; i < cols.length; i++) {
          header.createCell(i).setCellValue(cols[i]);
        }

        for (String sc : contracts) {
          BigDecimal totalM = aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes(sc, anomesM);
          BigDecimal totalM1 = aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes(sc, anomesM1);
          if (totalM == null) totalM = BigDecimal.ZERO;
          if (totalM1 == null) totalM1 = BigDecimal.ZERO;
          BigDecimal delta = totalM.subtract(totalM1);
          Range rng = baseline.get(sc);
          String status = evaluate(delta, rng);

          Row row = cons.createRow(r++);
          int c = 0;
          row.createCell(c++).setCellValue(nameBySc.getOrDefault(sc, "-"));
          row.createCell(c++).setCellValue(sc);
          row.createCell(c++).setCellValue(anomesM);
          row.createCell(c++).setCellValue(totalM.doubleValue());
          row.createCell(c++).setCellValue(anomesM1);
          row.createCell(c++).setCellValue(totalM1.doubleValue());
          row.createCell(c++).setCellValue(delta.doubleValue());
          row.createCell(c++).setCellValue(status);
          row.createCell(c++).setCellValue(hora);
          row.createCell(c++).setCellValue(data);

          // Analytical sheet for this service contract
          String analiticoName = sanitizeSheetName("Analítico_" + nameBySc.getOrDefault(sc, sc) + "_" + mmaa);
          if (analiticoName.length() > 31) analiticoName = analiticoName.substring(0, 31);
          Sheet ana = wb.createSheet(analiticoName);
          int ar = 0;
          Row ah = ana.createRow(ar++);
          String[] aCols = {"institutionNumber","serviceContract","anomes","merchantNumber","terminalId","valorCorrigido"};
          for (int i = 0; i < aCols.length; i++) {
            ah.createCell(i).setCellValue(aCols[i]);
          }
          // load both months
          List<AluguelProcessamentoAlianca> mList = aliancaRepo.findByServiceContractAndAnomes(sc, anomesM);
          List<AluguelProcessamentoAlianca> m1List = aliancaRepo.findByServiceContractAndAnomes(sc, anomesM1);
          List<AluguelProcessamentoAlianca> joined = new ArrayList<>();
          if (mList != null) joined.addAll(mList);
          if (m1List != null) joined.addAll(m1List);
          for (AluguelProcessamentoAlianca a : joined) {
            Row rr = ana.createRow(ar++);
            int cc = 0;
            AluguelId idBean = a.getId();
            rr.createCell(cc++).setCellValue(idBean != null ? safe(idBean.getInstitutionNumber()) : "");
            rr.createCell(cc++).setCellValue(idBean != null ? safe(idBean.getServiceContract()) : "");
            rr.createCell(cc++).setCellValue(idBean != null ? safe(idBean.getAnomes()) : "");
            rr.createCell(cc++).setCellValue(idBean != null ? safe(idBean.getMerchantNumber()) : "");
            rr.createCell(cc++).setCellValue(idBean != null ? safe(idBean.getTerminalId()) : "");
            BigDecimal v = a.getValorCorrigido();
            rr.createCell(cc++).setCellValue(v == null ? 0d : v.doubleValue());
          }
          for (int i = 0; i < aCols.length; i++) {
            ana.autoSizeColumn(i);
          }
        }
        for (int i = 0; i < 10; i++) cons.autoSizeColumn(i);

        Files.newOutputStream(out).write(toBytes(wb));
      }

      registry.put(id, JobStatus.DONE, out.toString(), null);
    } catch (Exception ex) {
      log.error("IPP job failed", ex);
      registry.put(id, JobStatus.ERROR, null, ex.getMessage());
    }
  }

  private String evaluate(BigDecimal delta, Range r) {
    int cmpMin = delta.compareTo(r.min);
    int cmpMax = delta.compareTo(r.max);
    if (cmpMin >= 0 && cmpMax <= 0) return "within-range";
    if (cmpMax > 0) return "above-range";
    return "below-range";
  }

  private String sanitizeSheetName(String name) {
    String sanitized = name.replace(':', '-')
        .replace('\\', '-')
        .replace('/', '-')
        .replace('?', '-')
        .replace('*', '-')
        .replace('[', '(')
        .replace(']', ')');
    sanitized = sanitized.replaceAll("^'+|'+$", "");
    return sanitized;
  }

  private static String safe(Object o) { return o == null ? "" : String.valueOf(o); }

  private byte[] toBytes(XSSFWorkbook wb) throws IOException {
    try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
      wb.write(bos);
      return bos.toByteArray();
    }
  }

  // --- Infra similar to CompareJobService ---
  @Configuration
  static class IppJobExecutorConfig {
    @Bean
    public Executor ippJobExecutor() {
      return defaultExecutor();
    }
  }

  private static Executor defaultExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(1);
    ex.setMaxPoolSize(1);
    ex.setQueueCapacity(10);
    ex.setThreadNamePrefix("ipp-job-");
    ex.initialize();
    return ex;
  }

  @Getter
  public enum JobStatus { RUNNING, DONE, ERROR }

  @Getter
  public static class JobInfo {
    private final JobStatus status;
    private final String filePath;
    private final String error;
    public JobInfo(JobStatus status, String filePath, String error) {
      this.status = status;
      this.filePath = filePath;
      this.error = error;
    }
  }

  public static class JobRegistry {
    private final java.util.Map<UUID, JobInfo> map = new java.util.concurrent.ConcurrentHashMap<>();
    public void put(UUID id, JobStatus status, String file, String error) {
      map.put(id, new JobInfo(status, file, error));
    }
    public JobInfo get(UUID id) { return map.get(id); }
  }

  private static class Range {
    final BigDecimal min;
    final BigDecimal max;
    Range(BigDecimal min, BigDecimal max) { this.min = min; this.max = max; }
  }
}
