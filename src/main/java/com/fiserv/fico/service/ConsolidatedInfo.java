package com.fiserv.fico.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedInfo {
  private String negocio; // alianca
  private String institution_number;
  private String service_contract;
  private int qtde_divergencias;
  private String hora;
  private String data;
  private String status; // OK or Verificar
}
