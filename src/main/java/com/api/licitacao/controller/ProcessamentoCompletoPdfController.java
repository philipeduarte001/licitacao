package com.api.licitacao.controller;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.dto.CapaItemDTO;
import com.api.licitacao.model.CotacaoDolar;
import com.api.licitacao.model.Fornecedor;
import com.api.licitacao.model.Produto;
import com.api.licitacao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.math3.dfp.DfpField;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/processamento")
@Tag(name = "Processamento Completo de PDFs", description = "Endpoints para processamento completo de documentos PDF com geração de planilhas Excel")
public class ProcessamentoCompletoPdfController {

    private final PdfReaderService pdfReaderService;
    private final FornecedorService fornecedorService;
    private final CotacaoDolarService cotacaoDolarService;
    private final CapaService capaService;
    private final AzureBlobService azureBlobService;

    public ProcessamentoCompletoPdfController(
            PdfReaderService pdfReaderService,
            FornecedorService fornecedorService,
            CotacaoDolarService cotacaoDolarService,
            CapaService capaService,
            AzureBlobService azureBlobService) {
        this.pdfReaderService = pdfReaderService;
        this.fornecedorService = fornecedorService;
        this.cotacaoDolarService = cotacaoDolarService;
        this.capaService = capaService;
        this.azureBlobService = azureBlobService;
    }

    @PostMapping(value = "/processar-pdfs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Processar múltiplos PDFs",
        description = "Processa uma lista de documentos PDF, extrai dados, busca fornecedores, obtém cotação do dólar e gera planilha Excel para download"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Planilha Excel gerada com sucesso",
                content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "400", description = "Nenhum PDF válido foi enviado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<byte[]> processarPdfsCompleto(
            @Parameter(
                description = "Lista de arquivos PDF para processamento", 
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("arquivos") List<MultipartFile> arquivos) {
        
        try {
            if (arquivos.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 0. Upload dos PDFs para Azure (extensão - não interfere na lógica existente)
            List<String> nomesBlobs = new ArrayList<>();
            try {
                if (azureBlobService.isConfigured()) {
                    nomesBlobs = azureBlobService.uploadMultiplosPdfs(arquivos);
                    System.out.println("Arquivos enviados para Azure: " + nomesBlobs);
                }
            } catch (Exception e) {
                System.err.println("Erro no upload para Azure (continuando processamento): " + e.getMessage());
                // Continua o processamento mesmo com erro no upload
            }

            // 1. Processar todos os PDFs e combinar os dados
            CapaDTO capaBase = null;
            List<CapaItemDTO> todosItens = new ArrayList<>();
            
            for (MultipartFile arquivo : arquivos) {
                if (arquivo.isEmpty() || !isPdfFile(arquivo)) {
                    continue;
                }
                
                // 1.1 Ler PDF usando PdfReaderService
                CapaDTO capaPdf = pdfReaderService.extrairDadosPdf(arquivo);
                
                // Usa o primeiro PDF como base para informações gerais
                if (capaBase == null) {
                    capaBase = capaPdf;
                }
                
                // 1.2 Buscar produtos e fornecedores para cada PDF
                Produto produto = criarProdutoDoCapa(capaPdf);
                List<Fornecedor> fornecedores = fornecedorService.buscarFornecedores(produto);
                
                // Converter fornecedores em itens da capa
                List<CapaItemDTO> itensParaPdf = criarItensDosFornecedores(fornecedores, todosItens.size());
                todosItens.addAll(itensParaPdf);
            }
            
            if (capaBase == null) {
                return ResponseEntity.badRequest()
                    .body("Nenhum PDF válido foi processado".getBytes());
            }

            // 3.2 Buscar cotação atual do dólar
            BigDecimal cotacaoDolar = buscarCotacaoDolarAtual();

            // Criar CapaDTO completo com todos os dados
            CapaDTO capaCompleta = new CapaDTO(
                capaBase.processo(),
                capaBase.dataHora(),
                capaBase.organ(),
                capaBase.headerTitle(),
                capaBase.portal(),
                capaBase.edital(),
                capaBase.cliente(),
                capaBase.objeto(),
                capaBase.modalidade(),
                capaBase.amostra(),
                capaBase.entrega(),
                capaBase.cr(),
                capaBase.atestado(),
                capaBase.impugnacao(),
                capaBase.obs(),
                cotacaoDolar,
                todosItens
            );

            // 3.3 Gerar planilha capa.xlsx usando CapaService
            byte[] planilhaBytes = capaService.generateCapa(capaCompleta);

            // Configurar headers para download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "capa_processada.xlsx");
            headers.setContentLength(planilhaBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(planilhaBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Erro ao processar PDFs: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Erro interno: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping(value = "/processar-pdf-unico", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Processar PDF único",
        description = "Processa um único documento PDF, extrai dados, busca fornecedores, obtém cotação do dólar e gera planilha Excel para download"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Planilha Excel gerada com sucesso",
                content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "400", description = "Arquivo deve ser um PDF válido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<byte[]> processarPdfUnico(
            @Parameter(
                description = "Arquivo PDF para processamento", 
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        try {
            if (arquivo.isEmpty() || !isPdfFile(arquivo)) {
                return ResponseEntity.badRequest()
                    .body("Arquivo deve ser um PDF válido".getBytes());
            }

            // 0. Upload do PDF para Azure (extensão - não interfere na lógica existente)
            String nomeBlob = null;
            try {
                if (azureBlobService.isConfigured()) {
                    nomeBlob = azureBlobService.uploadPdf(arquivo);
                    System.out.println("Arquivo enviado para Azure: " + nomeBlob);
                }
            } catch (Exception e) {
                System.err.println("Erro no upload para Azure (continuando processamento): " + e.getMessage());
                // Continua o processamento mesmo com erro no upload
            }

            // 1.1 Ler PDF usando PdfReaderService
            CapaDTO capaPdf = pdfReaderService.extrairDadosPdf(arquivo);
            
            // 1.2 Buscar produtos e fornecedores
            Produto produto = criarProdutoDoCapa(capaPdf);
            List<Fornecedor> fornecedores = fornecedorService.buscarFornecedores(produto);
            
            // Converter fornecedores em itens da capa
            List<CapaItemDTO> itens = criarItensDosFornecedores(fornecedores, 0);

            // 3.2 Buscar cotação atual do dólar
            BigDecimal cotacaoDolar = buscarCotacaoDolarAtual();

            // Criar CapaDTO completo
            CapaDTO capaCompleta = new CapaDTO(
                capaPdf.processo(),
                capaPdf.dataHora(),
                capaPdf.organ(),
                capaPdf.headerTitle(),
                capaPdf.portal(),
                capaPdf.edital(),
                capaPdf.cliente(),
                capaPdf.objeto(),
                capaPdf.modalidade(),
                capaPdf.amostra(),
                capaPdf.entrega(),
                capaPdf.cr(),
                capaPdf.atestado(),
                capaPdf.impugnacao(),
                capaPdf.obs(),
                cotacaoDolar,
                itens
            );

            // 3.3 Gerar planilha capa.xlsx
            byte[] planilhaBytes = capaService.generateCapa(capaCompleta);

            // Configurar headers para download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "capa_" + capaPdf.processo().replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx");
            headers.setContentLength(planilhaBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(planilhaBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Erro ao processar PDF: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Erro interno: " + e.getMessage()).getBytes());
        }
    }

    private boolean isPdfFile(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        String filename = arquivo.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }

    private Produto criarProdutoDoCapa(CapaDTO capa) {
        Produto produto = new Produto();
        produto.setDescricaoDetalhada(capa.objeto() != null ? capa.objeto() : "Produto padrão");
        produto.setQuantidadeTotal(1);
        produto.setUnidadeFornecimento("UN");
        produto.setValorTotal(1000.0); // Valor padrão para busca
        return produto;
    }

    private List<CapaItemDTO> criarItensDosFornecedores(List<Fornecedor> fornecedores, int numeroInicialItem) {
        List<CapaItemDTO> itens = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < fornecedores.size(); i++) {
            Fornecedor fornecedor = fornecedores.get(i);
            
            // Determinar se é fornecedor nacional ou importado
            String nacional;
            BigDecimal custoUnitario;
            BigDecimal frete;
            
            // Verificar se é o fornecedor americano
            if (fornecedor.getNome().contains("Tactical Gear USA")) {
                nacional = "Importado"; // Vazio = importado
                // Valores em dólar para o fornecedor americano
                double custoUSD = 25.0 + (random.nextDouble() * 75.0); // $25 a $100 USD
                double freteUSD = 15.0 + (random.nextDouble() * 35.0); // $15 a $50 USD
                custoUnitario = BigDecimal.valueOf(custoUSD);
                frete = BigDecimal.valueOf(freteUSD);
            } else {
                nacional = "Nacional";
                // Valores em reais para fornecedores nacionais
                double custoBase = 50.0 + (random.nextDouble() * 200.0); // R$ 50 a R$ 250
                double freteBase = 10.0 + (random.nextDouble() * 40.0); // R$ 10 a R$ 50
                custoUnitario = BigDecimal.valueOf(custoBase);
                frete = BigDecimal.valueOf(freteBase);
            }
            
            // Gerar quantidade aleatória
            int quantidade = random.nextInt(50) + 1; // 1 a 50 unidades
            
            CapaItemDTO item = new CapaItemDTO(
                numeroInicialItem + i + 1, // item número sequencial
                "Produto", // tipo
                fornecedor.getNome() + " - " + fornecedor.getObservacao(), // descrição
                quantidade,
                custoUnitario,
                frete,
                nacional
            );
            
            itens.add(item);
        }
        
        return itens;
    }

    private BigDecimal buscarCotacaoDolarAtual() {
        try {
            System.out.println("Buscando cotação atual do dólar via AwesomeAPI...");
            
            CotacaoDolar cotacao = cotacaoDolarService.getCotacaoDolar();
            
            if (cotacao != null && cotacao.getCotacao() != null && !cotacao.getCotacao().isEmpty()) {
                Double cotacaoVenda = Double.parseDouble(cotacao.getCotacao());
                System.out.println("Cotação encontrada: " + cotacaoVenda);
                return BigDecimal.valueOf(cotacaoVenda);
            } else {
                System.out.println("Cotação não encontrada na resposta da API");
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar cotação do dólar: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Valor padrão caso não encontre a cotação
        System.out.println("Usando cotação padrão: 5.50");
        return BigDecimal.valueOf(5.50);
    }
}