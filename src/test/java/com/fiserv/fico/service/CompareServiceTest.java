package com.fiserv.fico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fiserv.fico.repository.AluguelExcecaoRepository;
import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CompareServiceTest {

    @Autowired
    private CompareService service;

    @Autowired
    private AluguelProcessamentoAliancaRepository aliancaRepository;

    @Autowired
    private AluguelExcecaoRepository excecaoRepository;

    @BeforeEach
    void cleanDatabase() {
        aliancaRepository.deleteAll();
        excecaoRepository.deleteAll();
    }

    @Test
    void shouldReturnRecordsOnlyInAliancaWhenExcecaoIsEmpty() {
        aliancaRepository.save(alianca("001", "S1", "202401", "M-ONLY", "T-ONLY", new BigDecimal("100.50")));

        CompareReport report = service.compare("001", "S1", "202401");

        assertThat(report.onlyInAlianca())
                .hasSize(1)
                .first()
                .satisfies(record -> {
                    assertThat(record.getMerchantNumber()).isEqualTo("M-ONLY");
                    assertThat(record.getTerminalId()).isEqualTo("T-ONLY");
                    assertThat(record.getValorCorrigido()).isEqualByComparingTo("100.50");
                });
        assertThat(report.onlyInExcecao()).isEmpty();
        assertThat(report.divergent()).isEmpty();
    }

    @Test
    void shouldReturnRecordsOnlyInExcecaoWhenAliancaIsEmpty() {
        excecaoRepository.save(excecao("001", "S1", "202401", "M-ONLY", "D-ONLY", new BigDecimal("200.75")));

        CompareReport report = service.compare("001", "S1", "202401");

        assertThat(report.onlyInExcecao())
                .hasSize(1)
                .first()
                .satisfies(record -> {
                    assertThat(record.getMerchantNumber()).isEqualTo("M-ONLY");
                    assertThat(record.getDataValue()).isEqualTo("D-ONLY");
                    assertThat(record.getValRental()).isEqualByComparingTo("200.75");
                });
        assertThat(report.onlyInAlianca()).isEmpty();
        assertThat(report.divergent()).isEmpty();
    }

    @Test
    void shouldReturnDivergentRecordsWhenValuesDiffer() {
        aliancaRepository.save(alianca("001", "S1", "202401", "M-ALI", "T-ALI", new BigDecimal("300.00")));
        excecaoRepository.save(excecao("001", "S1", "202401", "M-EXC", "D-EXC", new BigDecimal("400.00")));

        CompareReport report = service.compare("001", "S1", "202401");

        assertThat(report.onlyInAlianca()).isEmpty();
        assertThat(report.onlyInExcecao()).isEmpty();
        assertThat(report.divergent())
                .hasSize(1)
                .first()
                .satisfies(divergent -> {
                    assertThat(divergent.key()).isEqualTo(new CompareKey("001", "S1", "202401"));
                    assertThat(divergent.diffs())
                            .hasSize(3)
                            .extracting(ValueDiff::field, ValueDiff::leftValue, ValueDiff::rightValue)
                            .containsExactlyInAnyOrder(
                                    tuple("merchantNumber", "M-ALI", "M-EXC"),
                                    tuple("terminalId", "T-ALI", "D-EXC"),
                                    tuple("valorCorrigido", "300.00", "400.00"));
                });
    }

    private AluguelProcessamentoAlianca alianca(
            String institution, String service, String anomes, String merchant, String terminal, BigDecimal valor) {
        AluguelProcessamentoAlianca entity = new AluguelProcessamentoAlianca();
        entity.setInstitutionNumber(institution);
        entity.setServiceContract(service);
        entity.setAnomes(anomes);
        entity.setMerchantNumber(merchant);
        entity.setTerminalId(terminal);
        entity.setValorCorrigido(valor);
        return entity;
    }

    private AluguelExcecao excecao(
            String institution, String service, String anomes, String merchant, String dataValue, BigDecimal valor) {
        AluguelExcecao entity = new AluguelExcecao();
        entity.setInstitutionNumber(institution);
        entity.setServiceContract(service);
        entity.setAnoMes(anomes);
        entity.setMerchantNumber(merchant);
        entity.setDataValue(dataValue);
        entity.setValRental(valor);
        return entity;
    }
}
