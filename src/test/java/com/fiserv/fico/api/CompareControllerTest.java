package com.fiserv.fico.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fiserv.fico.repository.AluguelExcecaoRepository;
import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompareControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnOnlyInAliancaRecords() throws Exception {
        aliancaRepository.save(alianca("001", "S1", "202401", "M-ONLY", "T-ONLY", new BigDecimal("100.50")));

        mockMvc.perform(get("/api/compare")
                        .param("institutionNumber", "001")
                        .param("serviceContract", "S1")
                        .param("anomes", "202401")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onlyInAlianca", hasSize(1)))
                .andExpect(jsonPath("$.onlyInAlianca[0].merchantNumber", is("M-ONLY")))
                .andExpect(jsonPath("$.onlyInAlianca[0].terminalId", is("T-ONLY")))
                .andExpect(jsonPath("$.onlyInAlianca[0].valorCorrigido", is(100.50)))
                .andExpect(jsonPath("$.onlyInExcecao", hasSize(0)))
                .andExpect(jsonPath("$.divergent", hasSize(0)));
    }

    @Test
    void shouldReturnOnlyInExcecaoRecords() throws Exception {
        excecaoRepository.save(excecao("001", "S1", "202401", "M-ONLY", "D-ONLY", new BigDecimal("200.75")));

        mockMvc.perform(get("/api/compare")
                        .param("institutionNumber", "001")
                        .param("serviceContract", "S1")
                        .param("anomes", "202401")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onlyInAlianca", hasSize(0)))
                .andExpect(jsonPath("$.onlyInExcecao", hasSize(1)))
                .andExpect(jsonPath("$.onlyInExcecao[0].merchantNumber", is("M-ONLY")))
                .andExpect(jsonPath("$.onlyInExcecao[0].dataValue", is("D-ONLY")))
                .andExpect(jsonPath("$.onlyInExcecao[0].valRental", is(200.75)))
                .andExpect(jsonPath("$.divergent", hasSize(0)));
    }

    @Test
    void shouldReturnDivergentRecords() throws Exception {
        aliancaRepository.save(alianca("001", "S1", "202401", "M-ALI", "T-ALI", new BigDecimal("300.00")));
        excecaoRepository.save(excecao("001", "S1", "202401", "M-EXC", "D-EXC", new BigDecimal("400.00")));

        mockMvc.perform(get("/api/compare")
                        .param("institutionNumber", "001")
                        .param("serviceContract", "S1")
                        .param("anomes", "202401")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onlyInAlianca", hasSize(0)))
                .andExpect(jsonPath("$.onlyInExcecao", hasSize(0)))
                .andExpect(jsonPath("$.divergent", hasSize(1)))
                .andExpect(jsonPath("$.divergent[0].key.institution", is("001")))
                .andExpect(jsonPath("$.divergent[0].key.service", is("S1")))
                .andExpect(jsonPath("$.divergent[0].key.anomes", is("202401")))
                .andExpect(jsonPath("$.divergent[0].diffs", hasSize(3)))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='merchantNumber')]", hasSize(1)))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='terminalId')]", hasSize(1)))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='valorCorrigido')]", hasSize(1)))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='merchantNumber')].leftValue", contains("M-ALI")))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='merchantNumber')].rightValue", contains("M-EXC")))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='terminalId')].leftValue", contains("T-ALI")))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='terminalId')].rightValue", contains("D-EXC")))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='valorCorrigido')].leftValue", contains("300.00")))
                .andExpect(jsonPath("$.divergent[0].diffs[?(@.field=='valorCorrigido')].rightValue", contains("400.00")));
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
