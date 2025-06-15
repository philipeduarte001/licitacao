package com.api.licitacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO para requisição ao serviço de processamento de PDF na nuvem
 */
public record CloudServiceRequestDTO(
    @JsonProperty("file_name")
    String fileName,
    
    @JsonProperty("container_name")
    String containerName,
    
    @JsonProperty("page_len")
    String pageLen,
    
    @JsonProperty("prompt_path")
    String promptPath,
    
    @JsonProperty("prompt_list")
    List<String> promptList
) {
    
    /**
     * Cria uma requisição padrão para processamento de edital
     * @param fileName Nome do arquivo PDF
     * @return CloudServiceRequestDTO configurado
     */
    public static CloudServiceRequestDTO createDefaultRequest(String fileName) {
        return new CloudServiceRequestDTO(
            fileName,
            "editals",
            "all",
            "./prompts",
            List.of("edital")
        );
    }
    
    /**
     * Cria uma requisição customizada
     * @param fileName Nome do arquivo PDF
     * @param containerName Nome do container
     * @param pageLen Quantidade de páginas
     * @param promptList Lista de prompts
     * @return CloudServiceRequestDTO configurado
     */
    public static CloudServiceRequestDTO createCustomRequest(
            String fileName, 
            String containerName, 
            String pageLen, 
            List<String> promptList) {
        return new CloudServiceRequestDTO(
            fileName,
            containerName,
            pageLen,
            "./prompts",
            promptList
        );
    }
} 