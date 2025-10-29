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
@Entity(name = "PRECO_JUSTO_SICREDI_ORIGEM_MERCHANT")
@Table(name = "PRECO_JUSTO_SICREDI_ORIGEM_MERCHANT", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediOrigemMerchant {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "MERCHANT_NUMBER")
    private String merchantNumber;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "CNPJ")
    private String cnpj;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "DATA_CREDENCIAMENTO")
    private String dataCredenciamento;

    @Column(name = "FATURAMENTO_PROMETIDO")
    private BigDecimal faturamentoPrometido;

    @Column(name = "DATA_BOARDING")
    private String dataBoarding;
}
