package com.api.licitacao.service;

import com.api.licitacao.model.Fornecedor;
import com.api.licitacao.model.Produto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Service
public class FornecedorService {

    public List<Fornecedor> buscarFornecedores(Produto produto) {

        Fornecedor mundoCarabina = new Fornecedor();
        mundoCarabina.setNome("Mundo da Carabina");
        mundoCarabina.setSite("https://www.mundodacarabina.com.br/");
        mundoCarabina.setTelefone("(41) 3022-7901 ou (41) 99808-1110");
        mundoCarabina.setEmail("contato@mundodacarabina.com.br");
        mundoCarabina.setObservacao("Alta probabilidade de ter lanternas táticas com especificações semelhantes");

        Fornecedor falconArmas = new Fornecedor();
        falconArmas.setNome("Falcon Armas");
        falconArmas.setSite("https://www.falconarmas.com.br/");
        falconArmas.setTelefone("(41) 3213-9100 ou (41) 98806-0361");
        falconArmas.setEmail("contato@falconarmas.com.br");
        falconArmas.setObservacao("Empresa com foco em equipamentos táticos e de aventura");

        Fornecedor casaPesca = new Fornecedor();
        casaPesca.setNome("Casa da Pesca");
        casaPesca.setSite("https://www.casadapesca.com.br/");
        casaPesca.setTelefone("(41) 3027-2110");
        casaPesca.setEmail("contato@casadapesca.com.br");
        casaPesca.setObservacao("Comercializa lanternas de alta performance e táticas");

        return Arrays.asList(mundoCarabina, falconArmas, casaPesca);
    }
} 