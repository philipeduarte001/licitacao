package com.api.licitacao.model;

import lombok.Data;
import java.util.List;

@Data
public class Fornecedor {
    private String nome;
    private String site;
    private String telefone;
    private String email;
    private String observacao;
    private List<ItemProduto> items;

    @Data
    public static class ItemProduto {
        private String tipo;
        private String descricao;
        private Double custoUnitario;
        private Double frete;

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public Double getCustoUnitario() {
            return custoUnitario;
        }

        public void setCustoUnitario(Double custoUnitario) {
            this.custoUnitario = custoUnitario;
        }

        public Double getFrete() {
            return frete;
        }

        public void setFrete(Double frete) {
            this.frete = frete;
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<ItemProduto> getItems() {
        return items;
    }

    public void setItems(List<ItemProduto> items) {
        this.items = items;
    }
}