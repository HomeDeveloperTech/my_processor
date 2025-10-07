package com.fiserv.fico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity(name = "ALUGUEL_PROCESSAMENTO_ALIANCA")
@Table(name = "ALUGUEL_PROCESSAMENTO_ALIANCA", schema = "#{app.oracle.schema}")
public class AluguelProcessamentoAlianca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "INSTITUTION_NUMBER", nullable = false)
    private String institutionNumber;

    @Column(name = "SERVICE_CONTRACT", nullable = false)
    private String serviceContract;

    @Column(name = "ANOMES", nullable = false)
    private String anomes;

    @Column(name = "MERCHANT_NUMBER")
    private String merchantNumber;

    @Column(name = "TERMINAL_ID")
    private String terminalId;

    @Column(name = "VALOR_CORRIGIDO")
    private BigDecimal valorCorrigido;

    public AluguelProcessamentoAlianca() {
        // JPA requirement
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstitutionNumber() {
        return institutionNumber;
    }

    public void setInstitutionNumber(String institutionNumber) {
        this.institutionNumber = institutionNumber;
    }

    public String getServiceContract() {
        return serviceContract;
    }

    public void setServiceContract(String serviceContract) {
        this.serviceContract = serviceContract;
    }

    public String getAnomes() {
        return anomes;
    }

    public void setAnomes(String anomes) {
        this.anomes = anomes;
    }

    public String getMerchantNumber() {
        return merchantNumber;
    }

    public void setMerchantNumber(String merchantNumber) {
        this.merchantNumber = merchantNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public BigDecimal getValorCorrigido() {
        return valorCorrigido;
    }

    public void setValorCorrigido(BigDecimal valorCorrigido) {
        this.valorCorrigido = valorCorrigido;
    }
}
