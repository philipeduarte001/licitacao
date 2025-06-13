package com.api.licitacao.controller;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.service.CapaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload de Documentos", description = "Endpoints para upload e extração de dados de documentos PDF")
public class UploadController {

    private final CapaService capaService;

    public UploadController(CapaService capaService) {
        this.capaService = capaService;
    }

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Extrair dados de múltiplos PDFs",
        description = "Extrai dados estruturados de uma lista de documentos PDF e retorna objetos CapaDTO"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados extraídos com sucesso",
                content = @Content(mediaType = "application/json", 
                        schema = @Schema(implementation = CapaDTO.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou não é PDF"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CapaDTO>> processarDocumentosPdf(
            @Parameter(
                description = "Lista de arquivos PDF para extração de dados", 
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("arquivos") List<MultipartFile> arquivos) {
        
        try {
            List<CapaDTO> resultados = new ArrayList<>();
            
            for (MultipartFile arquivo : arquivos) {
                if (arquivo.isEmpty()) {
                    continue;
                }
                
                // Valida se o arquivo é PDF
                if (!isPdfFile(arquivo)) {
                    throw new IllegalArgumentException(
                        "Arquivo " + arquivo.getOriginalFilename() + " não é um PDF válido");
                }
                
                // Processa o PDF e extrai os dados
                CapaDTO capaDTO = capaService.extrairDadosPdf(arquivo);
                resultados.add(capaDTO);
            }
            
            return ResponseEntity.ok(resultados);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PostMapping(value = "/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Extrair dados de PDF único",
        description = "Extrai dados estruturados de um documento PDF e retorna objeto CapaDTO"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados extraídos com sucesso",
                content = @Content(mediaType = "application/json", 
                        schema = @Schema(implementation = CapaDTO.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou não é PDF"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CapaDTO> processarDocumentoPdf(
            @Parameter(
                description = "Arquivo PDF para extração de dados", 
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        try {
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Valida se o arquivo é PDF
            if (!isPdfFile(arquivo)) {
                throw new IllegalArgumentException("Arquivo não é um PDF válido");
            }
            
            // Processa o PDF e extrai os dados
            CapaDTO capaDTO = capaService.extrairDadosPdf(arquivo);
            
            return ResponseEntity.ok(capaDTO);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    private boolean isPdfFile(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        String filename = arquivo.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
}