package com.api.licitacao.dto;

import java.math.BigDecimal;

public record ItemDTO(
    int item,
    String product,
    int quantity,
    int position,
    String empresa,
    String marcaMod,
    BigDecimal custo,
    BigDecimal valores,
    String nacional
) {}