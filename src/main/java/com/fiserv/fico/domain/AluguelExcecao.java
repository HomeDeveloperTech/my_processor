package com.fiserv.fico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Entity(name = "ALUGUEL_EXCECAO")
@Table(name = "ALUGUEL_EXCECAO", schema = "UAT_FICOIPB")
public class AluguelExcecao {

  @EmbeddedId
  private AluguelId id;

  @Column(name = "VALOR_EXCECAO")
  private BigDecimal valRental;

  // Convenience accessors to keep existing code working
  public String getInstitutionNumber() { return id != null ? id.getInstitutionNumber() : null; }
  public void setInstitutionNumber(String institutionNumber) {
    if (this.id == null) this.id = new AluguelId();
    this.id.setInstitutionNumber(institutionNumber);
  }
  public String getServiceContract() { return id != null ? id.getServiceContract() : null; }
  public void setServiceContract(String serviceContract) {
    if (this.id == null) this.id = new AluguelId();
    this.id.setServiceContract(serviceContract);
  }
  public String getAnomes() { return id != null ? id.getAnomes() : null; }
  public void setAnomes(String anomes) {
    if (this.id == null) this.id = new AluguelId();
    this.id.setAnomes(anomes);
  }
  public String getMerchantNumber() { return id != null ? id.getMerchantNumber() : null; }
  public void setMerchantNumber(String merchantNumber) {
    if (this.id == null) this.id = new AluguelId();
    this.id.setMerchantNumber(merchantNumber);
  }
  public String getTerminalId() { return id != null ? id.getTerminalId() : null; }
  public void setTerminalId(String terminalId) {
    if (this.id == null) this.id = new AluguelId();
    this.id.setTerminalId(terminalId);
  }
}
