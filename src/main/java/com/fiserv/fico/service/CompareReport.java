package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompareReport {
  private List<AluguelProcessamentoAlianca> onlyInAlianca;
  private List<AluguelExcecao> onlyInExcecao;
  private List<DivergentRecord> divergent;

  // Preserve record-style accessors
  public List<AluguelProcessamentoAlianca> onlyInAlianca() { return onlyInAlianca; }
  public List<AluguelExcecao> onlyInExcecao() { return onlyInExcecao; }
  public List<DivergentRecord> divergent() { return divergent; }
}
