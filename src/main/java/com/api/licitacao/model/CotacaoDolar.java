package com.api.licitacao.model;

import lombok.Data;
import java.util.List;

@Data
public class CotacaoDolar {
    private String odataContext;
    private List<CotacaoDolarValue> value;

    @Data
    public static class CotacaoDolarValue {
        private Double cotacaoCompra;
        private Double cotacaoVenda;
        private String dataHoraCotacao;

        public Double getCotacaoCompra() {
            return cotacaoCompra;
        }

        public void setCotacaoCompra(Double cotacaoCompra) {
            this.cotacaoCompra = cotacaoCompra;
        }

        public Double getCotacaoVenda() {
            return cotacaoVenda;
        }

        public void setCotacaoVenda(Double cotacaoVenda) {
            this.cotacaoVenda = cotacaoVenda;
        }

        public String getDataHoraCotacao() {
            return dataHoraCotacao;
        }

        public void setDataHoraCotacao(String dataHoraCotacao) {
            this.dataHoraCotacao = dataHoraCotacao;
        }
    }

    public String getOdataContext() {
        return odataContext;
    }

    public void setOdataContext(String odataContext) {
        this.odataContext = odataContext;
    }

    public List<CotacaoDolarValue> getValue() {
        return value;
    }

    public void setValue(List<CotacaoDolarValue> value) {
        this.value = value;
    }
}