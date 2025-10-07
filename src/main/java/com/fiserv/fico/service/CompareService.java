package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fiserv.fico.repository.AluguelExcecaoRepository;
import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CompareService {

    private final AluguelProcessamentoAliancaRepository aliancaRepository;
    private final AluguelExcecaoRepository excecaoRepository;

    public CompareService(
            AluguelProcessamentoAliancaRepository aliancaRepository,
            AluguelExcecaoRepository excecaoRepository) {
        this.aliancaRepository = aliancaRepository;
        this.excecaoRepository = excecaoRepository;
    }

    public CompareReport compare(String institution, String service, String anomes) {
        var alianca = aliancaRepository.findForCompare(institution, service, anomes);
        var excecao = excecaoRepository.findForCompare(institution, service, anomes);

        Map<CompareKey, AluguelProcessamentoAlianca> aliancaByKey = indexAlianca(alianca);
        Map<CompareKey, AluguelExcecao> excecaoByKey = indexExcecao(excecao);

        List<AluguelProcessamentoAlianca> onlyInAlianca = alianca.stream()
                .filter(a -> !excecaoByKey.containsKey(keyOf(a)))
                .toList();

        List<AluguelExcecao> onlyInExcecao = excecao.stream()
                .filter(e -> !aliancaByKey.containsKey(keyOf(e)))
                .toList();

        List<DivergentRecord> divergent = buildDivergentRecords(aliancaByKey, excecaoByKey);

        return new CompareReport(onlyInAlianca, onlyInExcecao, divergent);
    }

    private Map<CompareKey, AluguelProcessamentoAlianca> indexAlianca(
            List<AluguelProcessamentoAlianca> alianca) {
        Map<CompareKey, AluguelProcessamentoAlianca> map = new LinkedHashMap<>();
        for (AluguelProcessamentoAlianca item : alianca) {
            map.put(keyOf(item), item);
        }
        return map;
    }

    private Map<CompareKey, AluguelExcecao> indexExcecao(List<AluguelExcecao> excecoes) {
        Map<CompareKey, AluguelExcecao> map = new LinkedHashMap<>();
        for (AluguelExcecao item : excecoes) {
            map.put(keyOf(item), item);
        }
        return map;
    }

    private List<DivergentRecord> buildDivergentRecords(
            Map<CompareKey, AluguelProcessamentoAlianca> aliancaByKey,
            Map<CompareKey, AluguelExcecao> excecaoByKey) {
        Set<CompareKey> commonKeys = new LinkedHashSet<>(aliancaByKey.keySet());
        commonKeys.retainAll(excecaoByKey.keySet());

        List<DivergentRecord> divergent = new ArrayList<>();
        for (CompareKey key : commonKeys) {
            var alianca = aliancaByKey.get(key);
            var excecao = excecaoByKey.get(key);
            List<ValueDiff> diffs = new ArrayList<>();

            if (!Objects.equals(alianca.getMerchantNumber(), excecao.getMerchantNumber())) {
                diffs.add(new ValueDiff(
                        "merchantNumber",
                        toStringOrNull(alianca.getMerchantNumber()),
                        toStringOrNull(excecao.getMerchantNumber())));
            }

            if (!Objects.equals(alianca.getTerminalId(), excecao.getDataValue())) {
                diffs.add(new ValueDiff(
                        "terminalId",
                        toStringOrNull(alianca.getTerminalId()),
                        toStringOrNull(excecao.getDataValue())));
            }

            if (!equalBigDecimal(alianca.getValorCorrigido(), excecao.getValRental())) {
                diffs.add(new ValueDiff(
                        "valorCorrigido",
                        toStringOrNull(alianca.getValorCorrigido()),
                        toStringOrNull(excecao.getValRental())));
            }

            if (!diffs.isEmpty()) {
                divergent.add(new DivergentRecord(key, List.copyOf(diffs)));
            }
        }
        return List.copyOf(divergent);
    }

    private CompareKey keyOf(AluguelProcessamentoAlianca alianca) {
        return new CompareKey(
                alianca.getInstitutionNumber(),
                alianca.getServiceContract(),
                alianca.getAnomes());
    }

    private CompareKey keyOf(AluguelExcecao excecao) {
        return new CompareKey(
                excecao.getInstitutionNumber(),
                excecao.getServiceContract(),
                excecao.getAnoMes());
    }

    private boolean equalBigDecimal(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
