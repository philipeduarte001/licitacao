package com.api.licitacao.dto;

import java.time.LocalDate;
import java.util.List;

public record ResultadoDTO(
    String processNumber,
    String organ,
    LocalDate date,
    List<ItemDTO> items
) {}