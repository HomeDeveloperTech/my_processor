package com.fiserv.fico.api;

import com.fiserv.fico.service.IppJobService;
import com.fiserv.fico.service.IppJobService.JobInfo;
import com.fiserv.fico.service.IppJobService.JobStatus;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IppJobController.class)
class IppJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IppJobService jobService;

    @Test
    @DisplayName("POST /api/ipp/jobs should return jobId")
    void startJob() throws Exception {
        UUID id = UUID.randomUUID();
        when(jobService.startJob()).thenReturn(id);

        mockMvc.perform(post("/api/ipp/jobs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/ipp/jobs/{id} should return status map")
    void getStatus() throws Exception {
        UUID id = UUID.randomUUID();
        JobInfo info = new JobInfo(JobStatus.RUNNING, "", null);
        when(jobService.getJob(id)).thenReturn(info);

        mockMvc.perform(get("/api/ipp/jobs/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.file").value(""))
                .andExpect(jsonPath("$.error").value(""));
    }

    @Test
    @DisplayName("GET /api/ipp/jobs/{id}/file should download partial when RUNNING")
    void downloadPartial() throws Exception {
        // Create a temp XLSX file
        Path tmp = Files.createTempFile("ipp-partial-", ".xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Files.newOutputStream(tmp).write(toBytes(wb));
        }
        UUID id = UUID.randomUUID();
        when(jobService.getJob(id)).thenReturn(new JobInfo(JobStatus.RUNNING, tmp.toString(), null));

        String mmaa = String.format("%02d%02d", LocalDate.now().getMonthValue(), LocalDate.now().getYear() % 100);
        String expectedPrefix = "attachment; filename=\"" + URLEncoder.encode("PARCIAL_IPP-ALUGUEL_POSTADO_" + mmaa + ".xlsx", StandardCharsets.UTF_8) + "\"";

        mockMvc.perform(get("/api/ipp/jobs/{id}/file", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("PARCIAL_IPP-ALUGUEL_POSTADO_" + mmaa)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/ipp/jobs/{id}/file should download final when DONE")
    void downloadFinal() throws Exception {
        Path tmp = Files.createTempFile("ipp-final-", ".xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Files.newOutputStream(tmp).write(toBytes(wb));
        }
        UUID id = UUID.randomUUID();
        when(jobService.getJob(id)).thenReturn(new JobInfo(JobStatus.DONE, tmp.toString(), null));

        String mmaa = String.format("%02d%02d", LocalDate.now().getMonthValue(), LocalDate.now().getYear() % 100);

        mockMvc.perform(get("/api/ipp/jobs/{id}/file", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("FINAL_IPP_ALUGUEL_POSTADO_" + mmaa)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private byte[] toBytes(XSSFWorkbook wb) throws java.io.IOException {
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            wb.write(bos);
            return bos.toByteArray();
        }
    }
}
