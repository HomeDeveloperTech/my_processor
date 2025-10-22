package com.fiserv.fico.service;

import com.fiserv.fico.repository.AluguelProcessamentoAliancaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class IppAluguelPostadoServiceTest {

    private AluguelProcessamentoAliancaRepository repository;
    private IppAluguelPostadoService service;

    @BeforeEach
    void setUp() {
        repository = mock(AluguelProcessamentoAliancaRepository.class);
        service = new IppAluguelPostadoService(repository);
    }

    @Test
    @DisplayName("within-range for service 125 (0..300k) should complete without errors")
    void withinRangeBin125() {
        // Δ = 300000 → within (max)
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("125", "202501"))
                .thenReturn(new BigDecimal("2700000"));
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("125", "202412"))
                .thenReturn(new BigDecimal("2400000"));

        assertDoesNotThrow(() -> service.run(List.of("125"), "202501", "202412"));

        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("125", "202501");
        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("125", "202412");
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("above-range for service 110 (0..500k) should complete and not throw")
    void aboveRangeSicredi110() {
        // Δ = 600000 → above max
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("110", "202501"))
                .thenReturn(new BigDecimal("20600000"));
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("110", "202412"))
                .thenReturn(new BigDecimal("20000000"));

        assertDoesNotThrow(() -> service.run(List.of("110"), "202501", "202412"));

        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("110", "202501");
        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("110", "202412");
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("below-range for service 104 (-50k..0) should complete and not throw")
    void belowRangeSicoob104() {
        // Δ = -100000 → below min
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("104", "202501"))
                .thenReturn(new BigDecimal("100000"));
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("104", "202412"))
                .thenReturn(new BigDecimal("200000"));

        assertDoesNotThrow(() -> service.run(List.of("104"), "202501", "202412"));

        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("104", "202501");
        verify(repository).sumValorCorrigidoByServiceContractAndAnomes("104", "202412");
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("when repository throws, service should attempt isolated reprocess and still finish")
    void errorAndReprocessPath() {
        // First call throws, second attempt returns values
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("149", "202501"))
                .thenThrow(new RuntimeException("db error"))
                .thenReturn(new BigDecimal("7150000"));
        when(repository.sumValorCorrigidoByServiceContractAndAnomes("149", "202412"))
                .thenReturn(new BigDecimal("7500000"));

        assertDoesNotThrow(() -> service.run(List.of("149"), "202501", "202412"));

        // At least two calls to the failing method because of reprocess
        verify(repository, times(2)).sumValorCorrigidoByServiceContractAndAnomes("149", "202501");
        verify(repository, atLeastOnce()).sumValorCorrigidoByServiceContractAndAnomes("149", "202412");
        verifyNoMoreInteractions(repository);
    }
}
