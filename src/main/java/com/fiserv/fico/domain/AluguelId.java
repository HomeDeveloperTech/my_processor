package com.fiserv.fico.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite identifier for Aluguel entities based on business keys.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class AluguelId implements Serializable {

  @Column(name = "INSTITUTION_NUMBER", nullable = false)
  private String institutionNumber;

  @Column(name = "SERVICE_CONTRACT_ID", nullable = false)
  private String serviceContract;

  @Column(name = "ANOMES", nullable = false)
  private String anomes;

  @Column(name = "MERCHANT_NUMBER", nullable = false)
  private String merchantNumber;

  @Column(name = "TERMINAL_ID", nullable = false)
  private String terminalId;
}
