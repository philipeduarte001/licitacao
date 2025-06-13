package com.api.licitacao.service;


import com.api.licitacao.dto.CapaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CapaService {
    /**
     * Gera o arquivo Excel "capa.xlsx" preenchido
     * com base no DTO, mantendo layout, fontes, cores e fórmulas.
     */
    byte[] generateCapa(CapaDTO dto);

    /**
     * Extrai dados de um arquivo PDF e retorna um CapaDTO preenchido
     * com as informações encontradas no documento.
     */
    CapaDTO extrairDadosPdf(MultipartFile arquivo) throws IOException;
}
