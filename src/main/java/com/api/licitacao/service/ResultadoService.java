package com.api.licitacao.service;

import com.api.licitacao.dto.ResultadoDTO;

public interface ResultadoService {
    /**
     * Gera o arquivo Excel (byte[]) com base no DTO,
     * mantendo todo o layout e estilos do template original.
     */
    byte[] generateResultado(ResultadoDTO dto);
}
