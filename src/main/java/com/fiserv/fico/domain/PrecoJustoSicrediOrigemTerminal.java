package com.fiserv.fico.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PRECO_JUSTO_SICREDI_ORIGEM_TERMINAL")
@Table(name = "PRECO_JUSTO_SICREDI_ORIGEM_TERMINAL", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediOrigemTerminal {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "TERMINAL_ID")
    private String terminalId;

    @Column(name = "TERMINAL_TYPE")
    private String terminalType;

    @Column(name = "MERCHANT_NUMBER")
    private String merchantNumber;

    @Column(name = "INSTITUTION_NUMBER")
    private String institutionNumber;

    @Column(name = "SERVICE_CONTRACT")
    private String serviceContract;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "VALOR_ORIGEM")
    private BigDecimal valorOrigem;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "CNPJ")
    private String cnpj;
}
