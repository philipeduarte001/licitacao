package com.api.licitacao.model;

import lombok.Data;

@Data
public class Produto {
    private String descricaoDetalhada;
    private String tratamentoDiferenciado;
    private String aplicabilidadeDecreto;
    private Integer quantidadeTotal;
    private String criterioJulgamento;
    private String criterioValor;
    private Double valorTotal;
    private String unidadeFornecimento;
    private Double intervaloMinimoLances;

    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }

    public void setDescricaoDetalhada(String descricaoDetalhada) {
        this.descricaoDetalhada = descricaoDetalhada;
    }

    public String getTratamentoDiferenciado() {
        return tratamentoDiferenciado;
    }

    public void setTratamentoDiferenciado(String tratamentoDiferenciado) {
        this.tratamentoDiferenciado = tratamentoDiferenciado;
    }

    public String getAplicabilidadeDecreto() {
        return aplicabilidadeDecreto;
    }

    public void setAplicabilidadeDecreto(String aplicabilidadeDecreto) {
        this.aplicabilidadeDecreto = aplicabilidadeDecreto;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public String getCriterioJulgamento() {
        return criterioJulgamento;
    }

    public void setCriterioJulgamento(String criterioJulgamento) {
        this.criterioJulgamento = criterioJulgamento;
    }

    public String getCriterioValor() {
        return criterioValor;
    }

    public void setCriterioValor(String criterioValor) {
        this.criterioValor = criterioValor;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getUnidadeFornecimento() {
        return unidadeFornecimento;
    }

    public void setUnidadeFornecimento(String unidadeFornecimento) {
        this.unidadeFornecimento = unidadeFornecimento;
    }

    public Double getIntervaloMinimoLances() {
        return intervaloMinimoLances;
    }

    public void setIntervaloMinimoLances(Double intervaloMinimoLances) {
        this.intervaloMinimoLances = intervaloMinimoLances;
    }
}