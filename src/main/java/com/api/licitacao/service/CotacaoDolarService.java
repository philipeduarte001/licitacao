package com.api.licitacao.service;

import com.api.licitacao.model.CotacaoDolar;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CotacaoDolarService {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia";

    public CotacaoDolarService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CotacaoDolar getCotacaoDolar(String data) {
        String url = BASE_URL + "(dataCotacao=@dataCotacao)?@dataCotacao='" + data + "'&$format=json";
        return restTemplate.getForObject(url, CotacaoDolar.class);
    }
} 