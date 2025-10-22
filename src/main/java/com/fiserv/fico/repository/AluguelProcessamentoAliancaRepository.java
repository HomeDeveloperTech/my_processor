package com.fiserv.fico.repository;
import com.fiserv.fico.domain.AluguelId;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AluguelProcessamentoAliancaRepository
    extends JpaRepository<AluguelProcessamentoAlianca, AluguelId> {

  @Query("select a from ALUGUEL_PROCESSAMENTO_ALIANCA a " +
         "where a.id.institutionNumber = :institution " +
         "and a.id.serviceContract = :service " +
         "and a.id.anomes = :anomes")
  List<AluguelProcessamentoAlianca> findForCompare(
      @Param("institution") String institution,
      @Param("service") String service,
      @Param("anomes") String anomes);

  @Query("select coalesce(sum(a.valorCorrigido), 0) from ALUGUEL_PROCESSAMENTO_ALIANCA a " +
         "where a.id.serviceContract = :service and a.id.anomes = :anomes")
  BigDecimal sumValorCorrigidoByServiceContractAndAnomes(@Param("service") String service,
                                                         @Param("anomes") String anomes);

  @Query("select a from ALUGUEL_PROCESSAMENTO_ALIANCA a where a.id.serviceContract = :service and a.id.anomes = :anomes")
  List<AluguelProcessamentoAlianca> findByServiceContractAndAnomes(@Param("service") String service,
                                                                   @Param("anomes") String anomes);
}
