package com.fiserv.fico.api;

import com.fiserv.fico.service.IppAluguelPostadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ipp")
@Validated
@RequiredArgsConstructor
public class IppController {

  private final IppAluguelPostadoService service;

  @PostMapping("/run")
  @Operation(summary = "Executa o cálculo de Δ Aluguel Postado por service contract",
      description = "Calcula a variação do aluguel postado entre M e M-1, aplica baselines e emite logs/relatórios.")
  public String run(
      @RequestParam(required = false)
      @Parameter(description = "Lista de service contracts separados por vírgula. Se vazio, usa padrão [110,149,125,104]")
          String serviceContracts,
      @RequestParam(required = false)
      @Parameter(description = "Competência do mês atual (AAAAMM). Se vazio, usa mês corrente.")
          String anomesM,
      @RequestParam(required = false)
      @Parameter(description = "Competência do mês anterior (AAAAMM). Se vazio, usa mês anterior ao corrente.")
          String anomesM1) {

    if (anomesM == null && anomesM1 == null && (serviceContracts == null || serviceContracts.isBlank())) {
      service.runForCurrentMonth();
      return "IPP job started for current and previous month";
    }

    List<String> scList = null;
    if (serviceContracts != null && !serviceContracts.isBlank()) {
      scList = Arrays.stream(serviceContracts.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toList());
    }

    service.run(scList, anomesM, anomesM1);
    return "IPP job started for M=" + anomesM + " and M-1=" + anomesM1;
  }
}
