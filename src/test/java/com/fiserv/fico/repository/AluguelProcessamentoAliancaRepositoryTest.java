package com.fiserv.fico.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AluguelProcessamentoAliancaRepositoryTest {

    @Autowired
    private AluguelProcessamentoAliancaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindRecordsMatchingInstitutionServiceAndAnomes() {
        persistAlianca("001", "S1", "202401", "M-1", "T-1", new BigDecimal("10.50"));
        persistAlianca("001", "S1", "202401", "M-2", "T-2", new BigDecimal("20.00"));
        persistAlianca("002", "S1", "202401", "M-3", "T-3", new BigDecimal("30.00"));
        persistAlianca("001", "S2", "202401", "M-4", "T-4", new BigDecimal("40.00"));
        persistAlianca("001", "S1", "202402", "M-5", "T-5", new BigDecimal("50.00"));

        List<AluguelProcessamentoAlianca> result = repository.findForCompare("001", "S1", "202401");

        assertThat(result)
                .hasSize(2)
                .extracting(AluguelProcessamentoAlianca::getMerchantNumber)
                .containsExactlyInAnyOrder("M-1", "M-2");
    }

    private void persistAlianca(
            String institution, String service, String anomes, String merchant, String terminal, BigDecimal valor) {
        AluguelProcessamentoAlianca entity = new AluguelProcessamentoAlianca();
        entity.setInstitutionNumber(institution);
        entity.setServiceContract(service);
        entity.setAnomes(anomes);
        entity.setMerchantNumber(merchant);
        entity.setTerminalId(terminal);
        entity.setValorCorrigido(valor);
        entityManager.persistAndFlush(entity);
    }
}
