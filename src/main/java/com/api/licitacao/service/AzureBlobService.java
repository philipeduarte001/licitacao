package com.api.licitacao.service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AzureBlobService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobService.class);

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:licitacao-pdfs}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    /**
     * Inicializa o cliente Azure Blob Storage
     */
    private void initializeClient() {
        if (blobServiceClient == null && connectionString != null) {
            try {
                blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
                
                containerClient = blobServiceClient.getBlobContainerClient(containerName);
                
                // Cria o container caso não exista
                if (!containerClient.exists()) {
                    containerClient.create();
                    logger.info("Container '{}' criado com sucesso", containerName);
                }
                
                logger.info("Cliente Azure Blob Storage inicializado com sucesso");
            } catch (Exception e) {
                logger.error("Erro ao inicializar cliente Azure Blob Storage: {}", e.getMessage());
                throw new RuntimeException("Falha ao conectar com Azure Blob Storage", e);
            }
        }
    }

    /**
     * Faz upload de um arquivo PDF para Azure Blob Storage
     * @param arquivo Arquivo MultipartFile para upload
     * @return Nome do blob gravado no Azure
     * @throws IOException Se ocorrer erro durante o upload
     */
    public String uploadPdf(MultipartFile arquivo) throws IOException {
        if (connectionString == null || connectionString.trim().isEmpty()) {
            logger.warn("Connection string do Azure não configurada. Upload ignorado.");
            return null;
        }

        try {
            initializeClient();
            
            // Gerar nome único para o blob mantendo o nome original
            String nomeOriginal = arquivo.getOriginalFilename();
            String nomeArquivo = gerarNomeUnico(nomeOriginal);
            
            logger.info("Iniciando upload do arquivo '{}' como '{}'", nomeOriginal, nomeArquivo);
            
            // Obter o cliente do blob
            BlobClient blobClient = containerClient.getBlobClient(nomeArquivo);
            
            // Fazer upload do arquivo
            blobClient.upload(arquivo.getInputStream(), arquivo.getSize(), true);
            
            // Definir metadados
            BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType("application/pdf")
                .setContentDisposition("attachment; filename=\"" + nomeOriginal + "\"");
            
            blobClient.setHttpHeaders(headers);
            
            logger.info("Upload do arquivo '{}' concluído com sucesso. Blob: '{}'", nomeOriginal, nomeArquivo);
            
            return nomeArquivo;
            
        } catch (Exception e) {
            logger.error("Erro ao fazer upload do arquivo '{}' para Azure: {}", 
                arquivo.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("Falha no upload para Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Faz upload de múltiplos arquivos PDF para Azure Blob Storage
     * @param arquivos Lista de arquivos MultipartFile para upload
     * @return Lista com os nomes dos blobs gravados no Azure
     */
    public List<String> uploadMultiplosPdfs(List<MultipartFile> arquivos) {
        List<String> nomesBlobs = new ArrayList<>();
        
        if (connectionString == null || connectionString.trim().isEmpty()) {
            logger.warn("Connection string do Azure não configurada. Uploads ignorados.");
            return nomesBlobs;
        }

        for (MultipartFile arquivo : arquivos) {
            try {
                if (!arquivo.isEmpty() && isPdfFile(arquivo)) {
                    String nomeBlob = uploadPdf(arquivo);
                    if (nomeBlob != null) {
                        nomesBlobs.add(nomeBlob);
                    }
                }
            } catch (Exception e) {
                logger.error("Erro ao fazer upload do arquivo '{}': {}", 
                    arquivo.getOriginalFilename(), e.getMessage());
                // Continua o processamento dos outros arquivos
            }
        }
        
        return nomesBlobs;
    }

    /**
     * Gera um nome único para o blob mantendo o nome original
     * @param nomeOriginal Nome original do arquivo
     * @return Nome único para o blob
     */
    private String gerarNomeUnico(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
            nomeOriginal = "documento.pdf";
        }
        
        // Remover caracteres especiais do nome original
        String nomeBase = nomeOriginal.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        // Adicionar timestamp e UUID para garantir unicidade
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // Separar nome e extensão
        int pontoIndex = nomeBase.lastIndexOf('.');
        String nome = pontoIndex > 0 ? nomeBase.substring(0, pontoIndex) : nomeBase;
        String extensao = pontoIndex > 0 ? nomeBase.substring(pontoIndex) : ".pdf";
        
        return String.format("%s_%s_%s%s", nome, timestamp, uuid, extensao);
    }

    /**
     * Verifica se o arquivo é um PDF válido
     * @param arquivo Arquivo para verificação
     * @return true se for PDF, false caso contrário
     */
    private boolean isPdfFile(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        String filename = arquivo.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }

    /**
     * Verifica se o serviço Azure está configurado
     * @return true se estiver configurado, false caso contrário
     */
    public boolean isConfigured() {
        return connectionString != null && !connectionString.trim().isEmpty();
    }

    /**
     * Retorna o nome do container configurado
     * @return Nome do container
     */
    public String getContainerName() {
        return containerName;
    }
} 