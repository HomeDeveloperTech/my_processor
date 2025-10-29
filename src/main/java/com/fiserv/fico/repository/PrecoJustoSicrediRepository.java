package com.fiserv.fico.repository;

import com.fiserv.fico.domain.PrecoJustoSicredi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrecoJustoSicrediRepository extends JpaRepository<PrecoJustoSicredi, Long> {
    List<PrecoJustoSicredi> findByAnomesAndServiceContract(String anomes, String serviceContract);
}
