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
@Entity(name = "PRECO_JUSTO_SICREDI_ORIGEM_TAXAS_ANTECIPACAO")
@Table(name = "PRECO_JUSTO_SICREDI_ORIGEM_TAXAS_ANTECIPACAO", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediOrigemTaxasAntecipacao {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "CNPJ")
    private String cnpj;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "AUTOMATICA")
    private Integer automatica;

    @Column(name = "EVENTUAL")
    private BigDecimal eventual;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;
}
