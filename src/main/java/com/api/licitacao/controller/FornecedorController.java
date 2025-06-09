package com.api.licitacao.controller;

import com.api.licitacao.model.Fornecedor;
import com.api.licitacao.model.Produto;
import com.api.licitacao.service.FornecedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping("/buscar")
    public ResponseEntity<List<Fornecedor>> buscarFornecedores(@RequestBody Produto produto) {
        return ResponseEntity.ok(fornecedorService.buscarFornecedores(produto));
    }
} 