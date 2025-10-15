package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareJobService;
import com.fiserv.fico.service.CompareJobService.JobInfo;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare/jobs")
@RequiredArgsConstructor
public class CompareJobController {

  private final CompareJobService jobService;

  @PostMapping
  public Map<String, String> start() {
    UUID id = jobService.startJob();
    return Map.of("jobId", id.toString());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> status(@PathVariable("id") UUID id) {
    JobInfo info = jobService.getJob(id);
    if (info == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(Map.of(
        "status", info.getStatus().name(),
        "file", info.getFilePath() == null ? "" : info.getFilePath(),
        "error", info.getError() == null ? "" : info.getError()
    ));
  }

  @GetMapping("/{id}/file")
  public ResponseEntity<Resource> download(@PathVariable("id") UUID id) throws IOException {
    JobInfo info = jobService.getJob(id);
    if (info == null) {
      return ResponseEntity.notFound().build();
    }
    Resource res = null;
    if (info.getFilePath() != null && !info.getFilePath().isBlank()) {
      res = new org.springframework.core.io.FileSystemResource(info.getFilePath());
    }
    if (res == null || !res.exists()) {
      res = jobService.getResultFile(id);
    }
    if (res == null || !res.exists()) {
      return ResponseEntity.notFound().build();
    }

    String mmaa = String.format("%02d%02d", java.time.LocalDate.now().getMonthValue(), java.time.LocalDate.now().getYear() % 100);
    String base;
    switch (info.getStatus()) {
      case RUNNING:
        base = "PARCIAL_CONCILIACAO-ALUGUEL_" + mmaa;
        break;
      case DONE:
        base = "FINAL_CONCILIACAO-ALUGUEL_" + mmaa;
        break;
      default:
        return ResponseEntity.notFound().build();
    }
    String filename = base + ".xlsx";
    String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(res);
  }
}
