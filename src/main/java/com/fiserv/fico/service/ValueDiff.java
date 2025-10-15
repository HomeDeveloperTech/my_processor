package com.fiserv.fico.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ValueDiff {
  private String field;
  private String leftValue;
  private String rightValue;

  // Preserve record-style accessors
  public String field() { return field; }
  public String leftValue() { return leftValue; }
  public String rightValue() { return rightValue; }
}
