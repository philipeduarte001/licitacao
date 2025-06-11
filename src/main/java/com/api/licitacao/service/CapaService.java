package com.api.licitacao.service;


import com.api.licitacao.dto.CapaDTO;

public interface CapaService {
    /**
     * Gera o arquivo Excel “capa.xlsx” preenchido
     * com base no DTO, mantendo layout, fontes, cores e fórmulas.
     */
    byte[] generateCapa(CapaDTO dto);
}
