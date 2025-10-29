package com.fiserv.fico.api;

import com.fiserv.fico.service.PrecoJustoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/preco-justo")
@Validated
@RequiredArgsConstructor
public class PrecoJustoController {

  private final PrecoJustoService service;

  @PostMapping("/run")
  @Operation(summary = "Executa o processo Preço Justo",
      description = "Gera um arquivo Excel (XLSX) com 5 abas no diretório ./out, baseado na tabela PRECO_JUSTO_SICREDI: Visão por EC, Clientes elegíveis não enquadrados, Aluguel por tecnologia/terminal, MDR por bandeira e produto, Antecipação.")
  public ResponseEntity<Void> run(
      @RequestParam @Parameter(description = "Competência (AAAAMM)") String anomes,
      @RequestParam(name = "serviceContract") @Parameter(description = "Service Contract") String serviceContract) {
    service.generate(anomes, serviceContract, Path.of("./out"));
    return ResponseEntity.accepted().build();
  }
}
