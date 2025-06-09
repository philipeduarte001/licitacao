package com.api.licitacao.controller;

import com.api.licitacao.model.Fornecedor;
import com.api.licitacao.model.Produto;
import com.api.licitacao.service.FornecedorService;
import com.api.licitacao.service.ProcessadorPdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/processador")
public class ProcessadorPdfController {

    private final ProcessadorPdfService processadorPdfService;
    private final FornecedorService fornecedorService;

    public ProcessadorPdfController(ProcessadorPdfService processadorPdfService, FornecedorService fornecedorService) {
        this.processadorPdfService = processadorPdfService;
        this.fornecedorService = fornecedorService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<List<Fornecedor>> processarPdf(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        List<Produto> produtos = processadorPdfService.processarPdf(arquivo);

        Produto produtoParaBuscar;
        if (!produtos.isEmpty()) {
            produtoParaBuscar = produtos.get(0);
        } else {
            produtoParaBuscar = new Produto();
            produtoParaBuscar.setDescricaoDetalhada("Lanterna Tática");
        }

        List<Fornecedor> fornecedores = fornecedorService.buscarFornecedores(produtoParaBuscar);
        return ResponseEntity.ok(fornecedores);
    }
} 