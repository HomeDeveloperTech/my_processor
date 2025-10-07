package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareService;
import com.fiserv.fico.service.PagedCompareReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare")
@Validated
@RequiredArgsConstructor
public class CompareController {

  private final CompareService service;

  @GetMapping
  @Operation(
      summary = "Compara registros entre as tabelas de aluguel",
      description =
          "Realiza a comparação entre as bases da Aliança e de Exceções e retorna um resumo "
              + "paginarizado dos registros divergentes e exclusivos.")
  public PagedCompareReport compare(
      @RequestParam @NotBlank
          @Parameter(
              description = "Código da instituição a ser analisada",
              example = "1234567")
          String institutionNumber,
      @RequestParam @NotBlank
          @Parameter(
              description = "Contrato de serviço utilizado na pesquisa",
              example = "001")
          String serviceContract,
      @RequestParam @NotBlank
          @Parameter(description = "Competência no formato AAAAMM", example = "202401")
          String anomes,
      @RequestParam(defaultValue = "0")
          @Min(0)
          @Parameter(description = "Número da página a ser retornada", example = "0")
          int page,
      @RequestParam(defaultValue = "100")
          @Min(1)
          @Max(1000)
          @Parameter(description = "Quantidade de registros por página", example = "100")
          int size) {
    return PagedCompareReport.from(
        service.compare(institutionNumber, serviceContract, anomes), page, size);
  }
}
