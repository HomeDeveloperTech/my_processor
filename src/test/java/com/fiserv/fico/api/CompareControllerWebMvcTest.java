package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareService;
import com.fiserv.fico.service.PagedCompareReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompareController.class)
class CompareControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    CompareService service;

    @Test
    @DisplayName("Should return 400 when required params are missing and use ApiExceptionHandler")
    void shouldValidateRequestParams() throws Exception {
        mvc.perform(get("/api/compare")
                        .param("institutionNumber", "")
                        .param("serviceContract", "")
                        .param("anomes", ""))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.details['compare.institutionNumber']").value("must not be blank"))
                .andExpect(jsonPath("$.details['compare.serviceContract']").value("must not be blank"))
                .andExpect(jsonPath("$.details['compare.anomes']").value("must not be blank"));
    }

    @Test
    @DisplayName("Should return 200 and invoke service when params are valid")
    void shouldReturnOkWhenValid() throws Exception {
        Mockito.when(service.compare(eq("001"), eq("S1"), eq("202401")))
                .thenReturn(new com.fiserv.fico.service.CompareReport(java.util.List.of(), java.util.List.of(), java.util.List.of()));

        mvc.perform(get("/api/compare")
                        .param("institutionNumber", "001")
                        .param("serviceContract", "S1")
                        .param("anomes", "202401")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
