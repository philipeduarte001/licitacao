package com.api.licitacao.controller;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.service.CloudPdfProcessingService;
import com.api.licitacao.service.AzureBlobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloud-pdf")
@Tag(name = "Processamento PDF na Nuvem", description = "Endpoints para processamento de PDF via serviço externo na nuvem")
public class CloudPdfController {

    private final CloudPdfProcessingService cloudPdfProcessingService;
    private final AzureBlobService azureBlobService;

    public CloudPdfController(CloudPdfProcessingService cloudPdfProcessingService, AzureBlobService azureBlobService) {
        this.cloudPdfProcessingService = cloudPdfProcessingService;
        this.azureBlobService = azureBlobService;
    }

    @GetMapping("/status")
    @Operation(
        summary = "Verificar status do serviço",
        description = "Verifica se o serviço de processamento na nuvem está disponível"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status do serviço obtido com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> verificarStatus() {
        try {
            boolean serviceAvailable = cloudPdfProcessingService.isServiceAvailable();
            boolean connectionOk = cloudPdfProcessingService.testConnection();
            String serviceUrl = cloudPdfProcessingService.getServiceUrl();

            Map<String, Object> status = Map.of(
                "serviceEnabled", serviceAvailable,
                "connectionOk", connectionOk,
                "serviceUrl", serviceUrl,
                "status", (serviceAvailable && connectionOk) ? "ONLINE" : "OFFLINE"
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "serviceEnabled", false,
                "connectionOk", false,
                "serviceUrl", "N/A",
                "status", "ERROR",
                "error", e.getMessage()
            );
            return ResponseEntity.ok(error);
        }
    }

    @GetMapping("/azure-status")
    @Operation(
        summary = "Verificar status do Azure Blob Storage",
        description = "Verifica se o Azure Blob Storage está configurado e acessível"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status do Azure obtido com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> verificarStatusAzure() {
        try {
            boolean azureConfigured = azureBlobService.isConfigured();
            
            Map<String, Object> status = Map.of(
                "azureConfigured", azureConfigured,
                "containerName", azureBlobService.getContainerName(),
                "status", azureConfigured ? "CONFIGURADO" : "NÃO CONFIGURADO"
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "azureConfigured", false,
                "containerName", "N/A",
                "status", "ERROR",
                "error", e.getMessage()
            );
            return ResponseEntity.ok(error);
        }
    }

    @PostMapping("/processar")
    @Operation(
        summary = "Processar PDF via serviço na nuvem",
        description = "Processa um arquivo PDF usando o serviço externo na nuvem com parâmetros padrão"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CapaDTO> processarPdf(
            @Parameter(description = "Nome do arquivo PDF no Azure Blob Storage", required = true)
            @RequestParam("fileName") String fileName) {
        
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CapaDTO resultado = cloudPdfProcessingService.processarPdfNaNuvem(fileName);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/processar-customizado")
    @Operation(
        summary = "Processar PDF com parâmetros customizados",
        description = "Processa um arquivo PDF usando o serviço externo na nuvem com parâmetros personalizados"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CapaDTO> processarPdfCustomizado(
            @Parameter(description = "Nome do arquivo PDF", required = true)
            @RequestParam("fileName") String fileName,
            
            @Parameter(description = "Nome do container", required = false)
            @RequestParam(value = "containerName", defaultValue = "editals") String containerName,
            
            @Parameter(description = "Número de páginas", required = false)
            @RequestParam(value = "pageLen", defaultValue = "4") String pageLen,
            
            @Parameter(description = "Lista de prompts (separados por vírgula)", required = false)
            @RequestParam(value = "prompts", defaultValue = "edital") String prompts) {
        
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Converter string de prompts em lista
            List<String> promptList = List.of(prompts.split(","));

            CapaDTO resultado = cloudPdfProcessingService.processarPdfCustomizado(
                fileName, containerName, pageLen, promptList
            );
            
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/processar-json")
    @Operation(
        summary = "Processar PDF via JSON request",
        description = "Processa um arquivo PDF usando payload JSON completo"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "JSON inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CapaDTO> processarPdfJson(
            @RequestBody ProcessarPdfRequest request) {
        
        try {
            if (request.fileName() == null || request.fileName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CapaDTO resultado = cloudPdfProcessingService.processarPdfCustomizado(
                request.fileName(),
                request.containerName() != null ? request.containerName() : "editals",
                request.pageLen() != null ? request.pageLen() : "4",
                request.promptList() != null ? request.promptList() : List.of("edital")
            );
            
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/teste-json-response")
    @Operation(
        summary = "Testar parse de resposta JSON",
        description = "Testa o parse de uma resposta JSON de exemplo do serviço na nuvem"
    )
    public ResponseEntity<CapaDTO> testarJsonResponse() {
        try {
            // JSON de exemplo retornado pelo serviço na nuvem
            String jsonExample = """
                {
                    "extracted_clausules": {
                        "edital": [
                            {
                                "processo": "2024-C5D7D",
                                "dataHora": "14/05/2025 11:24",
                                "cliente": "Secretaria de Estado da Segurança Pública e Defesa Social do Estado do Espírito Santo",
                                "objeto": "Aquisição de 500 unidades de Lanternas Táticas para fortalecer a Polícia Civil do Estado do Espírito Santo - PCES, conforme a Meta 01/Etapa 04 do Plano de Trabalho do Convênio SENASP/MJSP Nº 952400/2023.",
                                "cotacaoDolar": null,
                                "items": [
                                    {
                                        "item": 1,
                                        "descricao": "Lanternas Táticas de Mão, conforme especificação do Termo de Referência",
                                        "quantidade": 500,
                                        "custoUnitario": 876.33,
                                        "frete": 0.0
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;

            // Usar método interno para testar o parse
            CapaDTO resultado = cloudPdfProcessingService.testarParseJson(jsonExample);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/upload-e-processar", consumes = "multipart/form-data")
    @Operation(
        summary = "Upload e processamento de PDF via serviço na nuvem",
        description = "Faz upload do PDF para Azure Blob Storage e processa via serviço na nuvem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CapaDTO> uploadEPprocessar(
            @Parameter(description = "Arquivo PDF para upload e processamento", required = true)
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        try {
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar se é um PDF
            String contentType = arquivo.getContentType();
            String filename = arquivo.getOriginalFilename();
            
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest().build();
            }

            // Fazer upload para Azure Blob Storage
            String nomeBlob = azureBlobService.uploadPdf(arquivo);
            
            if (nomeBlob == null) {
                return ResponseEntity.internalServerError().build();
            }

            // Processar via serviço na nuvem
            CapaDTO resultado = cloudPdfProcessingService.processarPdfNaNuvem(nomeBlob);
            
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DTO para requisição via JSON
     */
    public record ProcessarPdfRequest(
        String fileName,
        String containerName,
        String pageLen,
        List<String> promptList
    ) {}
} 