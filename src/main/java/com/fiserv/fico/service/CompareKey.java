package com.fiserv.fico.service;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CompareKey {
  private String institution;
  private String service;
  private String anomes;

  // Preserve record-style accessors
  public String institution() { return institution; }
  public String service() { return service; }
  public String anomes() { return anomes; }
}
