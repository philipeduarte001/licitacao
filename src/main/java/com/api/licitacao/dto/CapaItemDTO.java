package com.api.licitacao.dto;

import java.math.BigDecimal;

public record CapaItemDTO(
    int item,
    String tipo,
    String descricao,
    int quantidade,
    BigDecimal custoUnitario,
    BigDecimal frete
) {}
