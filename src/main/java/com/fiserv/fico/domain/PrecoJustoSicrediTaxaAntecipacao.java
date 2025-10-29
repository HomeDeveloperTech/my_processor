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
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PRECO_JUSTO_SICREDI_TAXA_ANTECIPACAO")
@Table(name = "PRECO_JUSTO_SICREDI_TAXA_ANTECIPACAO", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediTaxaAntecipacao {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "PRECO_JUSTO_SICREDI_ID")
    private Long precoJustoSicrediId;

    @Column(name = "AUTOMATICA")
    private Integer automatica;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "CNPJ")
    private String cnpj;

    @Column(name = "INSTITUTION_NUMBER")
    private String institutionNumber;

    @Column(name = "SERVICE_CONTRACT")
    private String serviceContract;

    @Column(name = "EVENTUAL")
    private BigDecimal eventual;
}
