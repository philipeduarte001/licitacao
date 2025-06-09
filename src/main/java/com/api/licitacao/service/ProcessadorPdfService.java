package com.api.licitacao.service;

import com.api.licitacao.model.Licitacao;
import com.api.licitacao.model.Produto;
import com.api.licitacao.repository.LicitacaoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProcessadorPdfService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessadorPdfService.class);

    private final FornecedorService fornecedorService;
    private final LicitacaoRepository licitacaoRepository;

    public ProcessadorPdfService(FornecedorService fornecedorService, LicitacaoRepository licitacaoRepository) {
        this.fornecedorService = fornecedorService;
        this.licitacaoRepository = licitacaoRepository;
    }

    public List<Produto> processarPdf(MultipartFile arquivo) throws IOException {
        List<Produto> produtos = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(arquivo.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);
            
            logger.info("Texto extraído do PDF: {}", texto);

            Licitacao licitacao = new Licitacao();

            String portal = extrairInformacao(texto, "(?i)Portal[\\s:]+(.*?)(?=\\n|$)");
            String edital = extrairInformacao(texto, "(?i)Edital[\\s:]+(.*?)(?=\\n|$)");
            String cliente = extrairInformacao(texto, "(?i)Cliente[\\s:]+(.*?)(?=\\n|$)");
            String objeto = extrairInformacao(texto, "(?i)Objeto[\\s:]+(.*?)(?=\\n|$)");
            String modalidade = extrairInformacao(texto, "(?i)Modalidade[\\s:]+(.*?)(?=\\n|$)");
            String entrega = extrairInformacao(texto, "(?i)Entrega[\\s:]+(.*?)(?=\\n|$)");
            String atestado = extrairInformacao(texto, "(?i)Atestado[\\s:]+(.*?)(?=\\n|$)");
            String amostra = extrairInformacao(texto, "(?i)Amostra[\\s:]+(.*?)(?=\\n|$)");
            String cr = extrairInformacao(texto, "(?i)CR[\\s:]+(.*?)(?=\\n|$)");
            String impugnacao = extrairInformacao(texto, "(?i)Impugnação[\\s:]+(.*?)(?=\\n|$)");
            String observacoes = extrairInformacao(texto, "(?i)Obs[\\s:]+(.*?)(?=\\n|$)");
            
            logger.info("Informações extraídas:");
            logger.info("Portal: {}", portal);
            logger.info("Edital: {}", edital);
            logger.info("Cliente: {}", cliente);
            logger.info("Objeto: {}", objeto);
            logger.info("Modalidade: {}", modalidade);
            logger.info("Entrega: {}", entrega);
            logger.info("Atestado: {}", atestado);
            logger.info("Amostra: {}", amostra);
            logger.info("CR: {}", cr);
            logger.info("Impugnação: {}", impugnacao);
            logger.info("Observações: {}", observacoes);
            
            licitacao.setPORTAL(portal);
            licitacao.setEDITAL(edital);
            licitacao.setCLIENTE(cliente);
            licitacao.setOBJETO(objeto);
            licitacao.setMODALIDADE(modalidade);
            licitacao.setENTREGA(entrega);
            licitacao.setATESTADO(atestado);
            licitacao.setAMOSTRA(amostra);
            licitacao.setCR(cr);
            licitacao.setIMPUGNACAO(impugnacao);
            licitacao.setOBS(observacoes);

            Licitacao licitacaoSalva = licitacaoRepository.save(licitacao);
            logger.info("Licitação salva com ID: {}", licitacaoSalva.getId());

            Pattern pattern = Pattern.compile("Descrição Detalhada: (.*?)(?=Tratamento Diferenciado:|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(texto);
            
            while (matcher.find()) {
                String descricao = matcher.group(1).trim();

                String tratamentoDiferenciado = extrairInformacao(texto, "Tratamento Diferenciado: (.*?)(?=\\n|$)");
                String aplicabilidadeDecreto = extrairInformacao(texto, "Aplicabilidade Decreto 7174/2010: (.*?)(?=\\n|$)");
                String quantidadeTotal = extrairInformacao(texto, "Quantidade Total: (\\d+)");
                String criterioJulgamento = extrairInformacao(texto, "Critério de Julgamento: (.*?)(?=\\n|$)");
                String criterioValor = extrairInformacao(texto, "Critério de Valor: (.*?)(?=\\n|$)");
                String valorTotal = extrairInformacao(texto, "Valor Total \\(R\\$\\): ([\\d.,]+)");
                String unidadeFornecimento = extrairInformacao(texto, "Unidade de Fornecimento: (.*?)(?=\\n|$)");
                String intervaloMinimoLances = extrairInformacao(texto, "Intervalo Mínimo entre Lances \\(R\\$\\): ([\\d.,]+)");
                
                Produto produto = new Produto();
                produto.setDescricaoDetalhada(descricao);
                produto.setTratamentoDiferenciado(tratamentoDiferenciado);
                produto.setAplicabilidadeDecreto(aplicabilidadeDecreto);
                produto.setQuantidadeTotal(Integer.parseInt(quantidadeTotal));
                produto.setCriterioJulgamento(criterioJulgamento);
                produto.setCriterioValor(criterioValor);
                produto.setValorTotal(Double.parseDouble(valorTotal.replace(".", "").replace(",", ".")));
                produto.setUnidadeFornecimento(unidadeFornecimento);
                produto.setIntervaloMinimoLances(Double.parseDouble(intervaloMinimoLances.replace(".", "").replace(",", ".")));
                
                produtos.add(produto);
            }
        }
        
        return produtos;
    }
    
    private String extrairInformacao(String texto, String padrao) {
        Pattern pattern = Pattern.compile(padrao, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(texto);
        String resultado = matcher.find() ? matcher.group(1).trim() : "";
        logger.debug("Extraindo informação com padrão '{}': {}", padrao, resultado);
        return resultado;
    }
} 