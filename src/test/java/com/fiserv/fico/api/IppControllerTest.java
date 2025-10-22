package com.fiserv.fico.api;

import com.fiserv.fico.service.IppAluguelPostadoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IppController.class)
class IppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IppAluguelPostadoService ippService;

    @Test
    @DisplayName("POST /api/ipp/run with no params triggers current month run")
    void runWithoutParams() throws Exception {
        doNothing().when(ippService).runForCurrentMonth();

        mockMvc.perform(post("/api/ipp/run"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("current and previous month")));

        verify(ippService).runForCurrentMonth();
    }

    @Test
    @DisplayName("POST /api/ipp/run with params parses list and forwards to service")
    void runWithParams() throws Exception {
        doNothing().when(ippService).run(anyList(), eq("202501"), eq("202412"));

        mockMvc.perform(post("/api/ipp/run")
                        .param("serviceContracts", "110, 149 ,125")
                        .param("anomesM", "202501")
                        .param("anomesM1", "202412"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("M=202501")));

        verify(ippService).run(List.of("110", "149", "125"), "202501", "202412");
    }
}
