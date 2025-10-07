package com.fiserv.fico.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiserv.fico.domain.AluguelExcecao;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AluguelExcecaoRepositoryTest {

  @Autowired private AluguelExcecaoRepository repository;

  @Autowired private TestEntityManager entityManager;

  @Test
  void shouldFindRecordsMatchingInstitutionServiceAndAnomes() {
    persistExcecao("001", "S1", "202401", "M-1", "D-1", new BigDecimal("10.50"));
    persistExcecao("001", "S1", "202401", "M-2", "D-2", new BigDecimal("20.00"));
    persistExcecao("002", "S1", "202401", "M-3", "D-3", new BigDecimal("30.00"));
    persistExcecao("001", "S2", "202401", "M-4", "D-4", new BigDecimal("40.00"));
    persistExcecao("001", "S1", "202402", "M-5", "D-5", new BigDecimal("50.00"));

    List<AluguelExcecao> result = repository.findForCompare("001", "S1", "202401");

    assertThat(result)
        .hasSize(2)
        .extracting(AluguelExcecao::getMerchantNumber)
        .containsExactlyInAnyOrder("M-1", "M-2");
  }

  private void persistExcecao(
      String institution,
      String service,
      String anomes,
      String merchant,
      String dataValue,
      BigDecimal valor) {
    AluguelExcecao entity = new AluguelExcecao();
    entity.setInstitutionNumber(institution);
    entity.setServiceContract(service);
    entity.setAnoMes(anomes);
    entity.setMerchantNumber(merchant);
    entity.setDataValue(dataValue);
    entity.setValRental(valor);
    entityManager.persistAndFlush(entity);
  }
}
