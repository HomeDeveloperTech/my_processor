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
@Entity(name = "ALUGUEL_EXCECAO")
@Table(name = "ALUGUEL_EXCECAO", schema = "#{app.oracle.schema}")
public class AluguelExcecao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Column(name = "INSTITUTION_NUMBER", nullable = false)
  private String institutionNumber;

  @Column(name = "SERVICE_CONTRACT", nullable = false)
  private String serviceContract;

  @Column(name = "ANO_MES", nullable = false)
  private String anoMes;

  @Column(name = "MERCHANT_NUMBER")
  private String merchantNumber;

  @Column(name = "DATA_VALUE")
  private String dataValue;

  @Column(name = "VAL_RENTAL")
  private BigDecimal valRental;
}
