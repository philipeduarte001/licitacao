package com.api.licitacao.controller;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.dto.ResultadoDTO;
import com.api.licitacao.service.CapaService;
import com.api.licitacao.service.ResultadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ResultadoService resultadoService;
    private final CapaService capaService;

    public ExcelController(ResultadoService resultadoService, CapaService capaService) {
        this.resultadoService = resultadoService;
        this.capaService = capaService;
    }

    @PostMapping(
        value = "/resultado",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<ByteArrayResource> downloadResultado(@RequestBody ResultadoDTO dto) {
        byte[] excel = resultadoService.generateResultado(dto);
        var resource = new ByteArrayResource(excel);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resultado.xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excel.length)
            .body(resource);
    }

    @PostMapping(
            value    = "/capa",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<ByteArrayResource> downloadCapa(@RequestBody CapaDTO dto) {
        byte[] file = capaService.generateCapa(dto);
        var resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"capa.xlsx\"")
                .contentType(MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(file.length)
                .body(resource);
    }
}
