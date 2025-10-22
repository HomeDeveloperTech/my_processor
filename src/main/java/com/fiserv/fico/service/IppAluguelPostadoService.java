package com.fiserv.fico.service;

import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por calcular a variação (Δ) do aluguel postado por service contract
 * e registrar logs com o resultado, conforme guideline IPB 2023.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IppAluguelPostadoService {

  private final AluguelProcessamentoAliancaRepository aliancaRepository;

  private static final DateTimeFormatter AAAAMM = DateTimeFormatter.ofPattern("yyyyMM");

  /** Baselines configurados por service contract: [min, max] do Δ (valor absoluto) */
  private static final Map<String, Range> BASELINES;

  /** Lista de service contracts a serem processados por padrão. */
  private static final List<String> DEFAULT_SERVICE_CONTRACTS;

  static {
    Map<String, Range> tmp = new HashMap<>();
    // Sicredi 110: 0 .. 500_000
    tmp.put("110", new Range(BigDecimal.ZERO, new BigDecimal("500000")));
    // Caixa 149: 0 .. 500_000
    tmp.put("149", new Range(BigDecimal.ZERO, new BigDecimal("500000")));
    // Bin 125: 0 .. 300_000
    tmp.put("125", new Range(BigDecimal.ZERO, new BigDecimal("300000")));
    // Sicoob/Sipag 104: -50_000 .. 0 (queda esperada)
    tmp.put("104", new Range(new BigDecimal("-50000"), BigDecimal.ZERO));
    BASELINES = Map.copyOf(tmp);

    DEFAULT_SERVICE_CONTRACTS = List.of("110", "149", "125", "104");
  }

  /**
   * Executa o cálculo para todos os service contracts padrão usando o mês atual e o anterior.
   */
  public void runForCurrentMonth() {
    LocalDate now = LocalDate.now();
    String m = now.format(AAAAMM);
    String m1 = now.minusMonths(1).format(AAAAMM);
    run(null, m, m1);
  }

  /**
   * Executa o cálculo para um conjunto de service contracts (ou padrão se null),
   * informando as competências M e M-1 (formato AAAAMM).
   */
  public void run(List<String> serviceContracts, String anomesM, String anomesM1) {
    List<String> contracts = (serviceContracts == null || serviceContracts.isEmpty())
        ? DEFAULT_SERVICE_CONTRACTS
        : new ArrayList<>(serviceContracts);

    AtomicInteger processed = new AtomicInteger();

    for (String sc : contracts) {
      try {
        processSingle(sc, anomesM, anomesM1);
        processed.incrementAndGet();
      } catch (Exception ex) {
        log.error("[IPP] Erro técnico ao processar serviceContract={} M={} M-1={}: {}", sc, anomesM, anomesM1, ex.toString(), ex);
        triggerReport("error", sc, anomesM, anomesM1, null, null, null, ex.getMessage());
        // Reprocessa apenas o contrato afetado
        try {
          processSingle(sc, anomesM, anomesM1);
          log.warn("[IPP] Reprocessamento com sucesso para serviceContract={} M={} M-1={}", sc, anomesM, anomesM1);
          triggerReport("reprocessed", sc, anomesM, anomesM1, null, null, null, null);
        } catch (Exception ex2) {
          log.error("[IPP] Falha no reprocessamento para serviceContract={} M={} M-1={}: {}", sc, anomesM, anomesM1, ex2.toString(), ex2);
          triggerReport("error", sc, anomesM, anomesM1, null, null, null, ex2.getMessage());
        }
      }
    }

    log.info("[IPP] Cálculo finalizado. Service contracts processados={}", processed.get());
    triggerReport("finished", null, anomesM, anomesM1, null, null, null, null);
  }

  private void processSingle(String serviceContract, String anomesM, String anomesM1) {
    // Exclusões por negócio: Afinz fora do escopo (sem número definido aqui). Caso venha a ser necessário,
    // incluir na lista abaixo.
    List<String> excluded = List.of();
    if (excluded.contains(serviceContract)) {
      log.info("[IPP] ServiceContract={} excluído das regras de cálculo (afinidades de negócio)", serviceContract);
      return;
    }

    Range baseline = Optional.ofNullable(BASELINES.get(serviceContract))
        .orElseThrow(() -> new IllegalArgumentException("Baseline não configurado para serviceContract=" + serviceContract));

    BigDecimal totalM = aliancaRepository.sumValorCorrigidoByServiceContractAndAnomes(serviceContract, anomesM);
    BigDecimal totalM1 = aliancaRepository.sumValorCorrigidoByServiceContractAndAnomes(serviceContract, anomesM1);

    BigDecimal delta = totalM.subtract(totalM1);

    String status;
    int cmpMin = delta.compareTo(baseline.min);
    int cmpMax = delta.compareTo(baseline.max);

    if (cmpMin >= 0 && cmpMax <= 0) {
      status = "within-range";
      log.info("[IPP] serviceContract={} M={} totalM={} M-1={} totalM1={} Δ={} -> Dentro da faixa {}..{}", serviceContract, anomesM, totalM, anomesM1, totalM1, delta, baseline.min, baseline.max);
    } else if (cmpMax > 0) {
      status = "above-range";
      log.warn("[IPP] serviceContract={} M={} totalM={} M-1={} totalM1={} Δ={} -> Fora da faixa (acima). Faixa {}..{}", serviceContract, anomesM, totalM, anomesM1, totalM1, delta, baseline.min, baseline.max);
    } else { // cmpMin < 0
      status = "below-range";
      log.warn("[IPP] serviceContract={} M={} totalM={} M-1={} totalM1={} Δ={} -> Fora da faixa (abaixo). Faixa {}..{}", serviceContract, anomesM, totalM, anomesM1, totalM1, delta, baseline.min, baseline.max);
    }

    triggerReport(status, serviceContract, anomesM, anomesM1, totalM, totalM1, delta, null);
  }

  private void triggerReport(String status,
                             String serviceContract,
                             String anomesM,
                             String anomesM1,
                             BigDecimal totalM,
                             BigDecimal totalM1,
                             BigDecimal delta,
                             String error) {
    // Gatilho via logs. Sistemas de observabilidade podem filtrar por essa chave.
    log.info("[IPP_REPORT_TRIGGER] status={} serviceContract={} M={} M-1={} totalM={} totalM1={} delta={} error={}",
        safe(status), safe(serviceContract), safe(anomesM), safe(anomesM1), safe(totalM), safe(totalM1), safe(delta), safe(error));
  }

  private static String safe(Object o) {
    return Objects.toString(o, "");
  }

  private static class Range {
    final BigDecimal min;
    final BigDecimal max;
    private Range(BigDecimal min, BigDecimal max) {
      this.min = min;
      this.max = max;
    }
  }
}
