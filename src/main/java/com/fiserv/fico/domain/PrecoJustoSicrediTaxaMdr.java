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
@Entity(name = "PRECO_JUSTO_SICREDI_TAXA_MDR")
@Table(name = "PRECO_JUSTO_SICREDI_TAXA_MDR", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediTaxaMdr {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "PRECO_JUSTO_SICREDI_ID")
    private Long precoJustoSicrediId;

    @Column(name = "BANDEIRA")
    private String bandeira;

    @Column(name = "CREDITO")
    private BigDecimal credito;

    @Column(name = "DEBITO")
    private BigDecimal debito;

    @Column(name = "PARCELADOMAIOR6")
    private BigDecimal parceladoMaior6;

    @Column(name = "PARCELADOMENORIGUAL6")
    private BigDecimal parceladoMenorIgual6;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "CNPJ")
    private String cnpj;
}
