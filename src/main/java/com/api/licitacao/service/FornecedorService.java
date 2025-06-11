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
        // Mock dos itens de produto
        Fornecedor.ItemProduto item1 = new Fornecedor.ItemProduto();
        item1.setTipo("Lanterna Tática");
        item1.setDescricao("Lanterna Tática Modelo X");
        item1.setCustoUnitario(45.50);
        item1.setFrete(200.00);

        Fornecedor.ItemProduto item2 = new Fornecedor.ItemProduto();
        item2.setTipo("Lanterna Tática");
        item2.setDescricao("Lanterna Tática Modelo Y");
        item2.setCustoUnitario(38.75);
        item2.setFrete(150.00);

        // Mock dos fornecedores
        Fornecedor mundoCarabina = new Fornecedor();
        mundoCarabina.setNome("Mundo da Carabina");
        mundoCarabina.setSite("https://www.mundodacarabina.com.br/");
        mundoCarabina.setTelefone("(41) 3022-7901 ou (41) 99808-1110");
        mundoCarabina.setEmail("contato@mundodacarabina.com.br");
        mundoCarabina.setObservacao("Alta probabilidade de ter lanternas táticas com especificações semelhantes");
        mundoCarabina.setItems(Arrays.asList(item1, item2));

        Fornecedor falconArmas = new Fornecedor();
        falconArmas.setNome("Falcon Armas");
        falconArmas.setSite("https://www.falconarmas.com.br/");
        falconArmas.setTelefone("(41) 3213-9100 ou (41) 98806-0361");
        falconArmas.setEmail("contato@falconarmas.com.br");
        falconArmas.setObservacao("Empresa com foco em equipamentos táticos e de aventura");
        falconArmas.setItems(Arrays.asList(item1));

        Fornecedor casaPesca = new Fornecedor();
        casaPesca.setNome("Casa da Pesca");
        casaPesca.setSite("https://www.casadapesca.com.br/");
        casaPesca.setTelefone("(41) 3027-2110");
        casaPesca.setEmail("contato@casadapesca.com.br");
        casaPesca.setObservacao("Comercializa lanternas de alta performance e táticas");
        casaPesca.setItems(Arrays.asList(item2));

        return Arrays.asList(mundoCarabina, falconArmas, casaPesca);
    }
} 