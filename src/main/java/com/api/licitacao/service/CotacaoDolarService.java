package com.api.licitacao.service;

import com.api.licitacao.model.CotacaoDolar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CotacaoDolarService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://economia.awesomeapi.com.br/last/USD-BRL";

    public CotacaoDolarService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public CotacaoDolar getCotacaoDolar() {
        try {
            String response = restTemplate.getForObject(BASE_URL, String.class);
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode usdbrlNode = rootNode.get("USDBRL");
                
                if (usdbrlNode != null) {
                    String code = usdbrlNode.get("code").asText();
                    String name = usdbrlNode.get("name").asText();
                    String ask = usdbrlNode.get("ask").asText();
                    String createDate = usdbrlNode.get("create_date").asText();
                    
                    return new CotacaoDolar(code, name, ask, createDate);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar resposta da API: " + e.getMessage());
        }
        
        // Retorna objeto vazio em caso de erro
        return new CotacaoDolar();
    }

    /**
     * Método para compatibilidade com código existente
     * @param data - Parâmetro ignorado, mantido para compatibilidade
     * @return Cotação atual do dólar
     */
    public CotacaoDolar getCotacaoDolar(String data) {
        return getCotacaoDolar();
    }
} 