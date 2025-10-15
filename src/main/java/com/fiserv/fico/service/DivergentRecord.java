package com.fiserv.fico.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DivergentRecord {
  private CompareKey key;
  private List<ValueDiff> diffs;

  // Preserve record-style accessors used in existing code
  public CompareKey key() { return key; }
  public List<ValueDiff> diffs() { return diffs; }
}
