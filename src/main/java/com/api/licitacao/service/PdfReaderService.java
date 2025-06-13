package com.api.licitacao.service;

import com.api.licitacao.dto.CapaDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfReaderService {

    // Padrões regex para extrair informações dos PDFs
    private static final Pattern PROCESSO_PATTERN = Pattern.compile("(?i)processo[\\s:\\-]*([\\w\\d\\-\\/\\.]+)");
    private static final Pattern ORGAO_PATTERN = Pattern.compile("(?i)(?:órgão|orgao)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern TITULO_PATTERN = Pattern.compile("(?i)(?:título|titulo|objeto)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern PORTAL_PATTERN = Pattern.compile("(?i)portal[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern EDITAL_PATTERN = Pattern.compile("(?i)edital[\\s:\\-]*([\\w\\d\\-\\/\\.]+)");
    private static final Pattern CLIENTE_PATTERN = Pattern.compile("(?i)cliente[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern OBJETO_PATTERN = Pattern.compile("(?i)objeto[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern MODALIDADE_PATTERN = Pattern.compile("(?i)modalidade[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern AMOSTRA_PATTERN = Pattern.compile("(?i)amostra[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern ENTREGA_PATTERN = Pattern.compile("(?i)(?:entrega|prazo)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern CR_PATTERN = Pattern.compile("(?i)(?:cr|centro de responsabilidade)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern ATESTADO_PATTERN = Pattern.compile("(?i)atestado[\\s:\\-]*(sim|não|yes|no|s|n)");
    private static final Pattern IMPUGNACAO_PATTERN = Pattern.compile("(?i)(?:impugnação|impugnacao)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern OBSERVACAO_PATTERN = Pattern.compile("(?i)(?:observações|observacoes|obs)[\\s:\\-]*([^\\n\\r]+)");
    private static final Pattern DATA_PATTERN = Pattern.compile("(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4})");
    private static final Pattern HORA_PATTERN = Pattern.compile("(\\d{1,2}:\\d{2})");

    public CapaDTO extrairDadosPdf(MultipartFile arquivo) throws IOException {
        try (PDDocument document = PDDocument.load(arquivo.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);
            
            return extrairInformacoes(texto);
        }
    }

    private CapaDTO extrairInformacoes(String texto) {
        // Extrai informações usando regex
        String processo = extrairTexto(texto, PROCESSO_PATTERN);
        String orgao = extrairTexto(texto, ORGAO_PATTERN);
        String headerTitle = extrairTexto(texto, TITULO_PATTERN);
        String portal = extrairTexto(texto, PORTAL_PATTERN);
        String edital = extrairTexto(texto, EDITAL_PATTERN);
        String cliente = extrairTexto(texto, CLIENTE_PATTERN);
        String objeto = extrairTexto(texto, OBJETO_PATTERN);
        String modalidade = extrairTexto(texto, MODALIDADE_PATTERN);
        String amostra = extrairTexto(texto, AMOSTRA_PATTERN);
        String entrega = extrairTexto(texto, ENTREGA_PATTERN);
        String cr = extrairTexto(texto, CR_PATTERN);
        String impugnacao = extrairTexto(texto, IMPUGNACAO_PATTERN);
        String obs = extrairTexto(texto, OBSERVACAO_PATTERN);
        
        // Extrai e processa atestado (boolean)
        boolean atestado = extrairAtestado(texto);
        
        // Extrai data e hora (se não encontrar, usa data/hora atual)
        LocalDateTime dataHora = extrairDataHora(texto);

        return new CapaDTO(
            processo != null ? processo : "",
            dataHora,
            orgao != null ? orgao : "",
            headerTitle != null ? headerTitle : "",
            portal != null ? portal : "",
            edital != null ? edital : "",
            cliente != null ? cliente : "",
            objeto != null ? objeto : "",
            modalidade != null ? modalidade : "",
            amostra != null ? amostra : "",
            entrega != null ? entrega : "",
            cr != null ? cr : "",
            atestado,
            impugnacao != null ? impugnacao : "",
            obs != null ? obs : "",
            null, // cotacaoDolar será preenchida pelo service principal se necessário
            null  // items pode ser preenchido por outro processo se necessário
        );
    }

    private String extrairTexto(String texto, Pattern pattern) {
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            String resultado = matcher.group(1).trim();
            // Remove quebras de linha e espaços extras
            resultado = resultado.replaceAll("[\\n\\r]+", " ").trim();
            return resultado.isEmpty() ? null : resultado;
        }
        return null;
    }

    private boolean extrairAtestado(String texto) {
        Matcher matcher = ATESTADO_PATTERN.matcher(texto);
        if (matcher.find()) {
            String valor = matcher.group(1).toLowerCase().trim();
            return valor.equals("sim") || valor.equals("yes") || valor.equals("s");
        }
        return false;
    }

    private LocalDateTime extrairDataHora(String texto) {
        String data = extrairTexto(texto, DATA_PATTERN);
        String hora = extrairTexto(texto, HORA_PATTERN);
        
        try {
            if (data != null) {
                // Normaliza o formato da data
                data = data.replaceAll("[/\\-]", "/");
                
                // Tenta diferentes formatos de data
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("d/M/yyyy"),
                    DateTimeFormatter.ofPattern("dd/MM/yy"),
                    DateTimeFormatter.ofPattern("d/M/yy")
                };
                
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        if (hora != null) {
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                                formatter.toString().replace("yyyy", "yyyy HH:mm").replace("yy", "yy HH:mm"));
                            return LocalDateTime.parse(data + " " + hora, dateTimeFormatter);
                        } else {
                            return LocalDateTime.parse(data + " 00:00", 
                                DateTimeFormatter.ofPattern(formatter.toString().replace("yyyy", "yyyy HH:mm").replace("yy", "yy HH:mm")));
                        }
                    } catch (Exception ignored) {
                        // Tenta próximo formato
                    }
                }
            }
        } catch (Exception e) {
            // Se não conseguir extrair data/hora, usa atual
        }
        
        return LocalDateTime.now();
    }
}