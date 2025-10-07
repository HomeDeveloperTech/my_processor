package com.fiserv.fico.repository;

import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AluguelProcessamentoAliancaRepository extends JpaRepository<AluguelProcessamentoAlianca, Long> {

    @Query("""
           select a from ALUGUEL_PROCESSAMENTO_ALIANCA a
           where a.institutionNumber = :institution
             and a.serviceContract = :service
             and a.anomes = :anomes
        """)
    List<AluguelProcessamentoAlianca> findForCompare(
            @Param("institution") String institution,
            @Param("service") String service,
            @Param("anomes") String anomes);
}
