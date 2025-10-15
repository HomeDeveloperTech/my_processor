package com.fiserv.fico.repository;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelId;
import com.fiserv.fico.domain.AluguelOrigem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AluguelOrigemRepository extends JpaRepository<AluguelOrigem, AluguelId> {

}
