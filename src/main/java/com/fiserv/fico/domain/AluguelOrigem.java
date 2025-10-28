package com.fiserv.fico.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Entity(name = "ALUGUEL_ORIGEM")
@Table(name = "ALUGUEL_ORIGEM", schema = "UAT_FICOIPB")
public class AluguelOrigem {

  @EmbeddedId
  private AluguelId id;

  @Column(name = "DEVICE_STATUS")
  private String deviceStatus;

  @Column(name = "MERCHANT_BOARDING_DATE")
  private String merchantBoardingDate;

  @Column(name = "SIGNED_SALES_VALUE")
  private BigDecimal signedSalesValue;

  @Column(name = "TERMINAL_INSTALATION_DATE")
  private String terminalInstalationDate;

  @Column(name = "DESC_TERMINAL_TYPE")
  private String descTerminalType;

  @Column(name = "VALOR_ORIGEM")
  private BigDecimal valorOrigem;

  @Column(name = "REGIS_NUM")
  private String regisNum;

  @Column(name = "ID_CLI_TYPE")
  private String idCliType;

  @Column(name = "FEE_CHARG_MODEL_CD")
  private Integer feeChargModelCd;

  @Column(name = "ID_PREPM")
  private String idPrepm;

  @Column(name = "MCC")
  private String mcc;

  @Column(name = "CAPTURE")
  private String capture;

  @Column(name = "ACTIVATION_DATE")
  private String activationDate;

  @Column(name = "CNAE")
  private String cnae;

  @Column(name = "GRUPO_ECONOMICO")
  private String grupoEconomico;

  @Column(name = "GRUPO_ECONOMICO_DESCRICAO")
  private String grupoEconomicoDescricao;

  @Column(name = "TERMINAL_TYPE")
  private String terminalType;

  @Column(name = "ALIANCA")
  private String alianca;

  @Column(name = "DT_INSERT_CONTROLE")
  private LocalDateTime dtInsertControle;
}
