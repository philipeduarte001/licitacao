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
    }
} 