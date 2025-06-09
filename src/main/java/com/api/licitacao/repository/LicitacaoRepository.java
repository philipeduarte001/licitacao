package com.api.licitacao.repository;

import com.api.licitacao.model.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicitacaoRepository extends JpaRepository<Licitacao, Long> {
} 