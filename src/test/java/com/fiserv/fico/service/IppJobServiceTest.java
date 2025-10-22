package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelId;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IppJobServiceTest {

    private IppAluguelPostadoService ippService;
    private AluguelProcessamentoAliancaRepository aliancaRepo;
    private IppJobService jobService;

    @BeforeEach
    void setUp() {
        ippService = mock(IppAluguelPostadoService.class);
        aliancaRepo = mock(AluguelProcessamentoAliancaRepository.class);
        jobService = new IppJobService(ippService, aliancaRepo);
    }

    @Test
    @DisplayName("startJob should complete and generate XLSX with expected sheets")
    void startJobGeneratesWorkbook() throws Exception {
        // Arrange current and previous months in both formats used by service
        LocalDate now = LocalDate.now();
        String mmaa = String.format("%02d%02d", now.getMonthValue(), now.getYear() % 100);
        String anomesM = String.format("%04d%02d", now.getYear(), now.getMonthValue());
        LocalDate prev = now.minusMonths(1);
        String anomesM1 = String.format("%04d%02d", prev.getYear(), prev.getMonthValue());

        // Mock IPP calculation trigger to be no-op
        doNothing().when(ippService).runForCurrentMonth();

        // Provide sums for all service contracts
        // 110, 149 within; 125 above; 104 below
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("110", anomesM)).thenReturn(new BigDecimal("20000000"));
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("110", anomesM1)).thenReturn(new BigDecimal("19800000")); // Δ=200k
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("149", anomesM)).thenReturn(new BigDecimal("7200000"));
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("149", anomesM1)).thenReturn(new BigDecimal("7000000")); // Δ=200k
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("125", anomesM)).thenReturn(new BigDecimal("2700000"));
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("125", anomesM1)).thenReturn(new BigDecimal("2300000")); // Δ=400k above 300k
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("104", anomesM)).thenReturn(new BigDecimal("100000"));
        when(aliancaRepo.sumValorCorrigidoByServiceContractAndAnomes("104", anomesM1)).thenReturn(new BigDecimal("200000")); // Δ=-100k below -50k

        // Analytical data (some rows for both months for a contract)
        when(aliancaRepo.findByServiceContractAndAnomes("110", anomesM)).thenReturn(List.of(
                buildAlianca("110", anomesM, "001", "T001", new BigDecimal("123.45"))
        ));
        when(aliancaRepo.findByServiceContractAndAnomes("110", anomesM1)).thenReturn(List.of(
                buildAlianca("110", anomesM1, "002", "T002", new BigDecimal("67.89"))
        ));
        // Default empty lists for others (avoid NPE)
        when(aliancaRepo.findByServiceContractAndAnomes(eq("149"), anyString())).thenReturn(List.of());
        when(aliancaRepo.findByServiceContractAndAnomes(eq("125"), anyString())).thenReturn(List.of());
        when(aliancaRepo.findByServiceContractAndAnomes(eq("104"), anyString())).thenReturn(List.of());

        // Act
        UUID jobId = jobService.startJob();
        assertNotNull(jobId);

        // Wait until job completes (max ~5s)
        IppJobService.JobInfo info = waitUntilDone(jobId, 5000);
        assertNotNull(info, "JobInfo should not be null");
        assertEquals(IppJobService.JobStatus.DONE, info.getStatus(), "Job should finish as DONE");
        assertNotNull(info.getFilePath());

        Path file = Path.of(info.getFilePath());
        assertTrue(Files.exists(file), "Output XLSX should exist");

        // Validate workbook content
        try (XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(file))) {
            // Consolidated sheet name
            assertNotNull(wb.getSheet("Consolidado_" + mmaa), "Should contain Consolidado sheet for month");
            // One analytical sheet should be created (SICREDI) - sheet name is sanitized but should start with Analítico_SICREDI
            boolean hasAnaliticoSicredi = wb.sheetIterator().hasNext();
            boolean found = false;
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                String name = wb.getSheetName(i);
                if (name.startsWith("Analítico_SICREDI_")) {
                    found = true; break;
                }
            }
            assertTrue(found, "Should contain an analytical sheet for SICREDI");
        }

        // Verify that service trigger was called
        verify(ippService, times(1)).runForCurrentMonth();
    }

    private IppJobService.JobInfo waitUntilDone(UUID id, long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        IppJobService.JobInfo info;
        do {
            info = jobService.getJob(id);
            if (info != null && (info.getStatus() == IppJobService.JobStatus.DONE || info.getStatus() == IppJobService.JobStatus.ERROR)) {
                return info;
            }
            Thread.sleep(50);
        } while (System.currentTimeMillis() < end);
        return info;
    }

    private AluguelProcessamentoAlianca buildAlianca(String sc, String anomes, String merchant, String terminal, BigDecimal valor) {
        AluguelId id = AluguelId.builder()
                .institutionNumber("INST")
                .serviceContract(sc)
                .anomes(anomes)
                .merchantNumber(merchant)
                .terminalId(terminal)
                .build();
        return AluguelProcessamentoAlianca.builder()
                .id(id)
                .valorCorrigido(valor)
                .build();
    }
}
