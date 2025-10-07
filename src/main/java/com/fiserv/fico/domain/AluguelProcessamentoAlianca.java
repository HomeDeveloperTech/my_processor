package com.fiserv.fico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Entity(name = "ALUGUEL_PROCESSAMENTO_ALIANCA")
@Table(name = "ALUGUEL_PROCESSAMENTO_ALIANCA", schema = "#{app.oracle.schema}")
public class AluguelProcessamentoAlianca {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Column(name = "INSTITUTION_NUMBER", nullable = false)
  private String institutionNumber;

  @Column(name = "SERVICE_CONTRACT", nullable = false)
  private String serviceContract;

  @Column(name = "ANOMES", nullable = false)
  private String anomes;

  @Column(name = "MERCHANT_NUMBER")
  private String merchantNumber;

  @Column(name = "TERMINAL_ID")
  private String terminalId;

  @Column(name = "VALOR_CORRIGIDO")
  private BigDecimal valorCorrigido;
}
