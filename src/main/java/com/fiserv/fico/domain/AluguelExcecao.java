package com.fiserv.fico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity(name = "ALUGUEL_EXCECAO")
@Table(name = "ALUGUEL_EXCECAO", schema = "#{app.oracle.schema}")
public class AluguelExcecao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "INSTITUTION_NUMBER", nullable = false)
    private String institutionNumber;

    @Column(name = "SERVICE_CONTRACT", nullable = false)
    private String serviceContract;

    @Column(name = "ANO_MES", nullable = false)
    private String anoMes;

    @Column(name = "MERCHANT_NUMBER")
    private String merchantNumber;

    @Column(name = "DATA_VALUE")
    private String dataValue;

    @Column(name = "VAL_RENTAL")
    private BigDecimal valRental;

    public AluguelExcecao() {
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

    public String getAnoMes() {
        return anoMes;
    }

    public void setAnoMes(String anoMes) {
        this.anoMes = anoMes;
    }

    public String getMerchantNumber() {
        return merchantNumber;
    }

    public void setMerchantNumber(String merchantNumber) {
        this.merchantNumber = merchantNumber;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public BigDecimal getValRental() {
        return valRental;
    }

    public void setValRental(BigDecimal valRental) {
        this.valRental = valRental;
    }
}
