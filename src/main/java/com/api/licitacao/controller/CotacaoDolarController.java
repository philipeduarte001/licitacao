package com.api.licitacao.controller;

import com.api.licitacao.model.CotacaoDolar;
import com.api.licitacao.service.CotacaoDolarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/cotacao")
public class CotacaoDolarController {

    private final CotacaoDolarService cotacaoDolarService;

    public CotacaoDolarController(CotacaoDolarService cotacaoDolarService) {
        this.cotacaoDolarService = cotacaoDolarService;
    }

    @GetMapping("/dolar")
    public ResponseEntity<CotacaoDolar> getCotacaoDolar(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate data) {
        String dataFormatada = data.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return ResponseEntity.ok(cotacaoDolarService.getCotacaoDolar(dataFormatada));
    }
} 