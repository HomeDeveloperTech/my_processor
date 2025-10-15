package com.fiserv.fico.repository;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AluguelExcecaoRepository extends JpaRepository<AluguelExcecao, AluguelId> {

  @Query("select e from ALUGUEL_EXCECAO e " +
         "where e.id.institutionNumber = :institution " +
         "and e.id.serviceContract = :service " +
         "and e.id.anomes = :anomes")
  List<AluguelExcecao> findForCompare(
      @Param("institution") String institution,
      @Param("service") String service,
      @Param("anomes") String anomes);
}
