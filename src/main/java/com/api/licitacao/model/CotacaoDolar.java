package com.api.licitacao.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CotacaoDolar {
    @JsonProperty("moeda")
    private String moeda;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("cotacao")
    private String cotacao;
    
    @JsonProperty("dataCotacao")
    private String dataCotacao;

    public CotacaoDolar() {
    }

    public CotacaoDolar(String moeda, String tipo, String cotacao, String dataCotacao) {
        this.moeda = moeda;
        this.tipo = tipo;
        this.cotacao = cotacao;
        this.dataCotacao = dataCotacao;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCotacao() {
        return cotacao;
    }

    public void setCotacao(String cotacao) {
        this.cotacao = cotacao;
    }

    public String getDataCotacao() {
        return dataCotacao;
    }

    public void setDataCotacao(String dataCotacao) {
        this.dataCotacao = dataCotacao;
    }
}