package com.fiserv.fico.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PRECO_JUSTO_SICREDI")
@Table(name = "PRECO_JUSTO_SICREDI", schema = "UAT_FICOIPB")
public class PrecoJustoSicredi {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "CNPJ")
    private String cnpj;

    @Column(name = "ANOMES")
    private String anomes;

    @Column(name = "QUANTIDADE_ENQUADRAMENTO")
    private Long quantidadeEnquadramento;

    @Column(name = "FLAG_ENQUADRAMENTO")
    private Integer flagEnquadramento;

    @Column(name = "FLAG_COMUNICADO")
    private Integer flagComunicado;

    @Column(name = "FLAG_MAJORADO")
    private Integer flagMajorado;

    @Column(name = "DATAINCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "FATURAMENTO_MEDIO_REALIZADO")
    private BigDecimal faturamentoMedioRealizado;

    @Column(name = "FATURAMENTO_COMBINADO")
    private BigDecimal faturamentoCombinado;

    @Column(name = "FATURAMENTO_COMBINADO_MIN")
    private BigDecimal faturamentoCombinadoMin;

    @Column(name = "FATURAMENTO_COMBINADO_MAX")
    private BigDecimal faturamentoCombinadoMax;

    @Column(name = "FATURAMENTO_MAJORACAO_MIN")
    private BigDecimal faturamentoMajoracaoMin;

    @Column(name = "FATURAMENTO_MAJORACAO_MAX")
    private BigDecimal faturamentoMajoracaoMax;

    @Column(name = "DATA_BOARDING")
    private String dataBoarding;

    @Column(name = "DATA_CREDENCIAMENTO")
    private String dataCredenciamento;

    @Column(name = "FLAG_BASE_INCLUIDO")
    private String flagBaseIncluido;

    @Column(name = "DATA_BASE_INCLUIDOS")
    private String dataBaseIncluidos;

    @Column(name = "DATA_BASE_EXCLUIDOS")
    private String dataBaseExcluidos;

    @Column(name = "FATURAMENTO_PROMETIDO_ANUAL")
    private BigDecimal faturamentoPrometidoAnual;

    @Column(name = "FLAG_BASE_EXCLUIDO")
    private String flagBaseExcluido;

    @Column(name = "FATURAMENTO_REALIZADO_M1")
    private BigDecimal faturamentoRealizadoM1;

    @Column(name = "FATURAMENTO_REALIZADO_M2")
    private BigDecimal faturamentoRealizadoM2;

    @Column(name = "FATURAMENTO_REALIZADO_M3")
    private BigDecimal faturamentoRealizadoM3;

    @Lob
    @Column(name = "REGRAS")
    private String regras;

    @Column(name = "RETURNCODE")
    private String returnCode;

    @Column(name = "SUCESSO")
    private String sucesso;

    @Column(name = "REGRA_APLICADA")
    private String regraAplicada;

    @Column(name = "FATURAMENTO_PROMETIDO_MENSAL")
    private BigDecimal faturamentoPrometidoMensal;

    @Column(name = "SERVICE_CONTRACT")
    private String serviceContract;

    @Column(name = "INSTITUTION_NUMBER")
    private String institutionNumber;
}
