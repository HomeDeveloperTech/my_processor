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
@Entity(name = "PRECO_JUSTO_SICREDI_TAXA_ALUGUEL")
@Table(name = "PRECO_JUSTO_SICREDI_TAXA_ALUGUEL", schema = "UAT_FICOIPB")
public class PrecoJustoSicrediTaxaAluguel {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "INSTITUTION_NUMBER")
    private String institutionNumber;

    @Column(name = "SERVICE_CONTRACT")
    private String serviceContract;

    @Column(name = "CNPJ")
    private String cnpj;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "PRECO_JUSTO_SICREDI_ID")
    private Long precoJustoSicrediId;

    @Column(name = "VALOR_ALUGUEL_CALCULANDO")
    private BigDecimal valorAluguelCalculando;

    @Column(name = "TERMINAL_TYPE")
    private String terminalType;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;
}
