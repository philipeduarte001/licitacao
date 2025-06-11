package com.api.licitacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CapaDTO(
    String processo,
    LocalDateTime dataHora,
    String organ,
    String headerTitle,
    String portal,
    String edital,
    String cliente,
    String objeto,
    String modalidade,
    String amostra,
    String entrega,
    String cr,
    boolean atestado,
    String impugnacao,
    String obs,
    BigDecimal cotacaoDolar,
    List<CapaItemDTO> items
) {}

