package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.util.List;

public record CompareReport(
    List<AluguelProcessamentoAlianca> onlyInAlianca,
    List<AluguelExcecao> onlyInExcecao,
    List<DivergentRecord> divergent) {}
