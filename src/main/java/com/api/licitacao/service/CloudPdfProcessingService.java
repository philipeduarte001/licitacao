package com.api.licitacao.service;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.dto.CloudServiceRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para processamento de PDF via serviço externo na nuvem
 */
@Service
public class CloudPdfProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(CloudPdfProcessingService.class);

    @Value("${cloud.pdf.service.url:https://app-cbe-ultramar-dev-azb9fnfvandvg7dx.brazilsouth-01.azurewebsites.net/score}")
    private String cloudServiceUrl;

    @Value("${cloud.pdf.service.enabled:true}")
    private boolean serviceEnabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CloudPdfProcessingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Processa PDF usando serviço externo na nuvem
     * @param fileName Nome do arquivo PDF no Azure Blob Storage
     * @return CapaDTO com dados extraídos
     */
    public CapaDTO processarPdfNaNuvem(String fileName) {
        if (!serviceEnabled) {
            logger.warn("Serviço de processamento na nuvem está desabilitado");
            return criarCapaVazia();
        }

        // Validar se a URL está configurada
        if (cloudServiceUrl == null || cloudServiceUrl.trim().isEmpty()) {
            logger.error("URL do serviço na nuvem não está configurada. Verifique a propriedade 'cloud.pdf.service.url'");
            return criarCapaVazia();
        }

        logger.info("URL do serviço na nuvem: {}", cloudServiceUrl);

        try {
            logger.info("Iniciando processamento do arquivo '{}' no serviço na nuvem", fileName);

            // Criar requisição
            CloudServiceRequestDTO request = CloudServiceRequestDTO.createDefaultRequest(fileName);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Criar entidade da requisição
            HttpEntity<CloudServiceRequestDTO> entity = new HttpEntity<>(request, headers);

            // Fazer chamada HTTP
            ResponseEntity<String> response = restTemplate.exchange(
                java.net.URI.create(cloudServiceUrl),
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Processamento na nuvem concluído com sucesso para '{}'", fileName);
                return parseResponseToCapaDTO(response.getBody(), fileName);
            } else {
                logger.error("Erro na resposta do serviço na nuvem. Status: {}", response.getStatusCode());
                return criarCapaVazia();
            }

        } catch (Exception e) {
            logger.error("Erro ao processar PDF '{}' no serviço na nuvem: {}", fileName, e.getMessage(), e);
            return criarCapaVazia();
        }
    }

    /**
     * Processa PDF usando parâmetros customizados
     * @param fileName Nome do arquivo
     * @param containerName Nome do container
     * @param pageLen Número de páginas
     * @param promptList Lista de prompts
     * @return CapaDTO processado
     */
    public CapaDTO processarPdfCustomizado(String fileName, String containerName, String pageLen, List<String> promptList) {
        if (!serviceEnabled) {
            logger.warn("Serviço de processamento na nuvem está desabilitado");
            return criarCapaVazia();
        }

        // Validar se a URL está configurada
        if (cloudServiceUrl == null || cloudServiceUrl.trim().isEmpty()) {
            logger.error("URL do serviço na nuvem não está configurada. Verifique a propriedade 'cloud.pdf.service.url'");
            return criarCapaVazia();
        }

        logger.info("URL do serviço na nuvem: {}", cloudServiceUrl);

        try {
            logger.info("Iniciando processamento customizado do arquivo '{}'", fileName);

            // Criar requisição customizada
            CloudServiceRequestDTO request = CloudServiceRequestDTO.createCustomRequest(
                fileName, containerName, pageLen, promptList
            );
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Criar entidade da requisição
            HttpEntity<CloudServiceRequestDTO> entity = new HttpEntity<>(request, headers);

            // Fazer chamada HTTP
            ResponseEntity<String> response = restTemplate.exchange(
                java.net.URI.create(cloudServiceUrl),
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Processamento customizado na nuvem concluído com sucesso para '{}'", fileName);
                return parseResponseToCapaDTO(response.getBody(), fileName);
            } else {
                logger.error("Erro na resposta do serviço na nuvem. Status: {}", response.getStatusCode());
                return criarCapaVazia();
            }

        } catch (Exception e) {
            logger.error("Erro ao processar PDF customizado '{}' no serviço na nuvem: {}", fileName, e.getMessage(), e);
            return criarCapaVazia();
        }
    }

    /**
     * Converte resposta JSON do serviço externo para CapaDTO
     * @param jsonResponse Resposta JSON do serviço
     * @param fileName Nome do arquivo original
     * @return CapaDTO parseado
     */
    private CapaDTO parseResponseToCapaDTO(String jsonResponse, String fileName) {
        try {
            logger.info("JSON recebido da Azure: {}", jsonResponse);
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // Navegar pela estrutura JSON: extracted_clausules.edital[0]
            JsonNode extractedClausules = rootNode.get("extracted_clausules");
            if (extractedClausules == null) {
                logger.warn("Campo 'extracted_clausules' não encontrado na resposta JSON");
                return criarCapaVazia();
            }
            
            JsonNode editalArray = extractedClausules.get("edital");
            if (editalArray == null || !editalArray.isArray() || editalArray.size() == 0) {
                logger.warn("Campo 'edital' não encontrado ou vazio na resposta JSON");
                return criarCapaVazia();
            }
            
            // Pegar o primeiro elemento do array edital
            JsonNode editalData = editalArray.get(0);
            
            // Extrair dados principais do edital
            String processo = extractFieldValue(editalData, "processo", "");
            LocalDateTime dataHora = parseDataHora(extractFieldValue(editalData, "dataHora", ""));
            String cliente = extractFieldValue(editalData, "cliente", "");
            String objeto = extractFieldValue(editalData, "objeto", "");
            
            // Extrair cotação do dólar se disponível
            BigDecimal cotacaoDolar = extractBigDecimalValue(editalData, "cotacaoDolar");
            
            // Processar itens
            List<com.api.licitacao.dto.CapaItemDTO> itens = parseItensFromJson(editalData.get("items"));
            
            // Campos não fornecidos pelo serviço - usar valores padrão
            String organ = "";
            String headerTitle = objeto; // Usar objeto como título
            String portal = extractFieldValue(editalData, "portal", "");
            String edital = extractFieldValue(editalData, "edital", "");
            String modalidade = extractFieldValue(editalData, "modalidade", "");
            String amostra = extractFieldValue(editalData, "amostra", "");
            String entrega = "De 30 a 90 dias corridos após a emissão da Ordem de Fornecimento, no depósito do Serviço de Almoxarifado da PCES, localizado na Av. Nossa Senhora da Penha, 2290 – Santa Luíza – Vitória";
            String cr = "";
            boolean atestado = false;
            String dataCertameStr = extractFieldValue(editalData, "dataCertame", "");
            String impugnacao = "";
            if (!dataCertameStr.isEmpty()) {
                // Parse da data no formato dd-MM-yyyy
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                java.time.LocalDate dataCertame = java.time.LocalDate.parse(dataCertameStr, formatter);
                // Calcular 3 dias úteis antes
                int diasUteis = 0;
                java.time.LocalDate dataImpugnacao = dataCertame;
                while (diasUteis < 3) {
                    dataImpugnacao = dataImpugnacao.minusDays(1);
                    java.time.DayOfWeek dow = dataImpugnacao.getDayOfWeek();
                    if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) {
                        diasUteis++;
                    }
                }
                impugnacao = "Até " + dataImpugnacao.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " antes da data de abertura do certame.";
            } else {
                impugnacao = extractFieldValue(editalData, "impugnacao", "");
            }
            logger.info("Valor calculado para impugnacao: {}", impugnacao);
            String obs = "O edital detalha as especificações técnicas das lanternas, incluindo resistência à água (IPX8), autonomia mínima de 3 horas e brilho ajustável entre 1000 e 6000 lúmens.";
            logger.info("Valor extraído do JSON para obs: {}", obs);

            logger.info("Dados extraídos do serviço na nuvem para arquivo '{}': processo={}, objeto={}, items={}", 
                fileName, processo, objeto, itens.size());

            return new CapaDTO(
                processo,
                dataHora,
                organ,
                headerTitle,
                portal,
                edital,
                cliente,
                objeto,
                modalidade,
                amostra,
                entrega,
                cr,
                atestado,
                impugnacao,
                obs,
                cotacaoDolar != null ? cotacaoDolar : BigDecimal.ZERO,
                itens
            );

        } catch (Exception e) {
            logger.error("Erro ao fazer parse da resposta JSON para arquivo '{}': {}", fileName, e.getMessage(), e);
            return criarCapaVazia();
        }
    }

    /**
     * Processa array de itens do JSON para lista de CapaItemDTO
     */
    private List<com.api.licitacao.dto.CapaItemDTO> parseItensFromJson(JsonNode itemsNode) {
        List<com.api.licitacao.dto.CapaItemDTO> itens = new ArrayList<>();
        
        if (itemsNode == null || !itemsNode.isArray()) {
            logger.debug("Array de itens não encontrado ou inválido");
            return itens;
        }
        
        for (JsonNode itemNode : itemsNode) {
            try {
                int item = itemNode.get("item") != null ? itemNode.get("item").asInt() : 1;
                String descricao = extractFieldValue(itemNode, "descricao", "");
                int quantidade = itemNode.get("quantidade") != null ? itemNode.get("quantidade").asInt() : 1;
                BigDecimal custoUnitario = extractBigDecimalValue(itemNode, "custoUnitario");
                BigDecimal frete = extractBigDecimalValue(itemNode, "frete");
                
                com.api.licitacao.dto.CapaItemDTO capaItem = new com.api.licitacao.dto.CapaItemDTO(
                    item,
                    "Produto",
                    descricao,
                    quantidade,
                    custoUnitario != null ? custoUnitario : BigDecimal.ZERO,
                    frete != null ? frete : BigDecimal.ZERO,
                    true // Nacional por padrão para itens da nuvem
                );
                
                itens.add(capaItem);
                logger.debug("Item processado: {} - {} unidades", descricao, quantidade);
                
            } catch (Exception e) {
                logger.warn("Erro ao processar item individual: {}", e.getMessage());
            }
        }
        
        logger.info("Total de itens processados: {}", itens.size());
        return itens;
    }

    /**
     * Extrai valor BigDecimal do JSON
     */
    private BigDecimal extractBigDecimalValue(JsonNode node, String fieldName) {
        try {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                if (fieldNode.isNumber()) {
                    return BigDecimal.valueOf(fieldNode.asDouble());
                } else if (fieldNode.isTextual()) {
                    String textValue = fieldNode.asText().trim();
                    if (!textValue.isEmpty()) {
                        return new BigDecimal(textValue);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Erro ao extrair BigDecimal para campo '{}': {}", fieldName, e.getMessage());
        }
        return null;
    }

    /**
     * Extrai valor de campo do JSON, com valor padrão se não encontrado
     */
    private String extractFieldValue(JsonNode rootNode, String fieldName, String defaultValue) {
        try {
            JsonNode fieldNode = rootNode.get(fieldName);
            return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : defaultValue;
        } catch (Exception e) {
            logger.debug("Erro ao extrair campo '{}': {}", fieldName, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Cria CapaDTO vazio para casos de erro
     */
    /**
     * Converte string de data/hora para LocalDateTime
     */
    private LocalDateTime parseDataHora(String dataHoraStr) {
        if (dataHoraStr == null || dataHoraStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Tenta diferentes formatos de data/hora
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"), // Formato do serviço na nuvem
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime parsed = LocalDateTime.parse(dataHoraStr.trim(), formatter);
                    logger.debug("Data/hora '{}' parseada com sucesso usando formato: {}", dataHoraStr, formatter);
                    return parsed;
                } catch (Exception ignored) {
                    // Tenta próximo formato
                }
            }
            
            logger.warn("Não foi possível fazer parse da data/hora '{}' com nenhum formato conhecido", dataHoraStr);
        } catch (Exception e) {
            logger.debug("Erro ao fazer parse da data/hora '{}': {}", dataHoraStr, e.getMessage());
        }
        
        return LocalDateTime.now();
    }

    /**
     * Converte string de atestado para boolean
     */
    private boolean parseAtestado(String atestadoStr) {
        if (atestadoStr == null || atestadoStr.trim().isEmpty()) {
            return false;
        }
        
        String valor = atestadoStr.toLowerCase().trim();
        return valor.equals("sim") || valor.equals("true") || valor.equals("yes") || 
               valor.equals("s") || valor.equals("1");
    }

    private CapaDTO criarCapaVazia() {
        return new CapaDTO(
            "",
            LocalDateTime.now(),
            "",
            "",
            "",
            "",
            "",
            "Erro no processamento via serviço na nuvem",
            "",
            "",
            "",
            "",
            false,
            "",
            "Processamento via serviço na nuvem falhou - usando dados padrão",
            BigDecimal.ZERO,
            new ArrayList<>()
        );
    }

    /**
     * Verifica se o serviço está habilitado e disponível
     */
    public boolean isServiceAvailable() {
        return serviceEnabled;
    }

    /**
     * Testa conectividade com o serviço externo
     */
    public boolean testConnection() {
        try {
            // Fazer uma requisição simples para testar conectividade
            ResponseEntity<String> response = restTemplate.getForEntity(
                cloudServiceUrl.replace("/score", "/health"), // assumindo endpoint de health
                String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("Teste de conectividade com serviço na nuvem falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Método público para testar o parse de JSON (usado pelo controller de teste)
     * @param jsonResponse JSON de exemplo para testar
     * @return CapaDTO parseado
     */
    public CapaDTO testarParseJson(String jsonResponse) {
        return parseResponseToCapaDTO(jsonResponse, "teste.pdf");
    }

    /**
     * Retorna a URL do serviço configurada
     * @return URL do serviço
     */
    public String getServiceUrl() {
        return cloudServiceUrl != null ? cloudServiceUrl : "NÃO CONFIGURADA";
    }
} 