package com.api.licitacao.service;

import com.api.licitacao.dto.CapaDTO;
import com.api.licitacao.dto.CapaItemDTO;
import com.api.licitacao.exception.ExcelGenerationException;
import com.api.licitacao.model.CotacaoDolar;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CapaServiceImpl implements CapaService {

    private static final String TEMPLATE_PATH = "templates/capa.xlsx";
    private static final DateTimeFormatter DT_FMT = 
        DateTimeFormatter.ofPattern("dd/MM 'ÀS' HH:mm'H'");
    
    private final CotacaoDolarService cotacaoDolarService;

    public CapaServiceImpl(CotacaoDolarService cotacaoDolarService) {
        this.cotacaoDolarService = cotacaoDolarService;
    }

    @Override
    public byte[] generateCapa(CapaDTO dto) {
        try (InputStream is = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.getSheetAt(0);
            
            // Se não foi fornecida cotação no DTO, busca automaticamente
            CapaDTO dtoComCotacao = dto;
            if (dto.cotacaoDolar() == null) {
                System.out.println("Cotação não fornecida no DTO, buscando automaticamente...");
                BigDecimal cotacaoAtual = buscarCotacaoDolarAtual();
                if (cotacaoAtual != null) {
                    dtoComCotacao = new CapaDTO(
                        dto.processo(), dto.dataHora(), dto.organ(), dto.headerTitle(),
                        dto.portal(), dto.edital(), dto.cliente(), dto.objeto(),
                        dto.modalidade(), dto.amostra(), dto.entrega(), dto.cr(),
                        dto.atestado(), dto.impugnacao(), dto.obs(), cotacaoAtual, dto.items()
                    );
                    System.out.println("Cotação do dólar obtida: " + cotacaoAtual);
                } else {
                    System.out.println("Não foi possível obter a cotação do dólar");
                }
            } else {
                System.out.println("Usando cotação fornecida no DTO: " + dto.cotacaoDolar());
            }
            
            fillHeader(sheet, dtoComCotacao);
            fillItems(sheet, dtoComCotacao.items());
            fillCotacaoDolar(wb, sheet, dtoComCotacao);

            wb.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new ExcelGenerationException("Falha ao gerar capa.xlsx", e);
        }
    }

    private void fillHeader(XSSFSheet s, CapaDTO dto) {
        s.getRow(0).getCell(2)
            .setCellValue(dto.processo() + " - " + dto.dataHora().format(DT_FMT));
        s.getRow(1).getCell(2).setCellValue(dto.organ());
        s.getRow(2).getCell(2).setCellValue(dto.headerTitle());
        s.getRow(4).getCell(3).setCellValue(dto.portal());
        s.getRow(4).getCell(9).setCellValue(dto.edital());
        s.getRow(5).getCell(3).setCellValue(dto.cliente());
        s.getRow(6).getCell(3).setCellValue(dto.objeto());
        s.getRow(7).getCell(3).setCellValue(dto.modalidade());
        s.getRow(7).getCell(9).setCellValue(dto.amostra());
        s.getRow(8).getCell(3).setCellValue(dto.entrega());
        s.getRow(8).getCell(9).setCellValue(dto.cr());
        s.getRow(9).getCell(3).setCellValue(dto.atestado() ? "SIM" : "NÃO");
        s.getRow(9).getCell(9).setCellValue(dto.impugnacao());
        s.getRow(10).getCell(3).setCellValue(dto.obs());
        
        // Adiciona a cotação do dólar - assumindo que está na linha 11, coluna 3 (ou ajuste conforme necessário)
        if (dto.cotacaoDolar() != null) {
            s.getRow(8).getCell(9).setCellValue(dto.cotacaoDolar().toString());
        }
    }

    private void fillItems(XSSFSheet s, List<CapaItemDTO> items) {
        final int BASE_DATA_ROW    = 13; // índice zero-based da 14ª linha
        final int BASE_FORMULA_ROW = 14;

        for (int i = 0; i < items.size(); i++) {
            int dr = BASE_DATA_ROW + i * 2;
            int fr = BASE_FORMULA_ROW + i * 2;
            if (i > 0) {
                cloneRow(s, BASE_DATA_ROW, dr);
                cloneRow(s, BASE_FORMULA_ROW, fr);
            }
            CapaItemDTO it = items.get(i);
            XSSFRow row = s.getRow(dr);
            setCell(row, 0, it.item());
            setCell(row, 1, it.tipo());
            setCell(row, 2, it.descricao());
            setCell(row, 3, it.quantidade());
            setCell(row, 4, it.custoUnitario().doubleValue());
            setCell(row, 7, it.frete().doubleValue());
        }
    }

    private void setCell(XSSFRow row, int col, String v) {
        row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
           .setCellValue(v);
    }
    private void setCell(XSSFRow row, int col, int v) {
        row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
           .setCellValue(v);
    }
    private void setCell(XSSFRow row, int col, double v) {
        row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
           .setCellValue(v);
    }

    private void cloneRow(XSSFSheet sheet, int src, int dest) {
        sheet.shiftRows(dest, sheet.getLastRowNum(), 1);
        XSSFRow srcRow = sheet.getRow(src);
        XSSFRow destRow = sheet.createRow(dest);
        destRow.setHeight(srcRow.getHeight());
        for (int c = srcRow.getFirstCellNum(); c < srcRow.getLastCellNum(); c++) {
            XSSFCell old = srcRow.getCell(c);
            if (old == null) continue;
            XSSFCell nw = destRow.createCell(c);
            nw.setCellStyle(old.getCellStyle());
            switch (old.getCellType()) {
                case STRING  -> nw.setCellValue(old.getStringCellValue());
                case NUMERIC -> nw.setCellValue(old.getNumericCellValue());
                case BOOLEAN -> nw.setCellValue(old.getBooleanCellValue());
                case FORMULA -> nw.setCellFormula(old.getCellFormula());
                default      -> nw.setBlank();
            }
        }
    }

    private void fillCotacaoDolar(XSSFWorkbook wb, XSSFSheet sheet, CapaDTO dto) {
        try {
            Double cotacaoVenda = null;
            
            // Usa a cotação do DTO se disponível
            if (dto.cotacaoDolar() != null) {
                cotacaoVenda = dto.cotacaoDolar().doubleValue();
                System.out.println("Usando cotação do DTO: " + cotacaoVenda);
            } else {
                // Fallback: busca a cotação do dólar para a data atual
                String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                System.out.println("Buscando cotação para a data: " + dataAtual);
                CotacaoDolar cotacao = cotacaoDolarService.getCotacaoDolar(dataAtual);
                
                if (cotacao != null && !cotacao.getValue().isEmpty()) {
                    cotacaoVenda = cotacao.getValue().get(0).getCotacaoVenda();
                    System.out.println("Cotação obtida da API BACEN: " + cotacaoVenda);
                }
            }
            
            if (cotacaoVenda != null) {
                boolean cellUpdated = false;
                
                // Primeiro, tenta procurar por célula nomeada "DOLAR"
                XSSFName namedRange = wb.getName("DOLAR");
                if (namedRange != null) {
                    System.out.println("Encontrou célula nomeada DOLAR");
                    cellUpdated = updateNamedCell(namedRange, sheet, cotacaoVenda);
                } else {
                    System.out.println("Não encontrou célula nomeada DOLAR");
                }
                
                // Se não encontrou célula nomeada, procura por célula que contenha "DOLAR" no texto
                if (!cellUpdated) {
                    System.out.println("Procurando célula por conteúdo DOLAR...");
                    updateCellByContent(sheet, cotacaoVenda);
                }
                
                System.out.println("Processo de preenchimento da cotação concluído");
            } else {
                System.out.println("Nenhuma cotação disponível para preencher");
            }
        } catch (Exception e) {
            // Em caso de erro na busca da cotação, continua sem interromper a geração da planilha
            System.err.println("Erro ao processar cotação do dólar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean updateNamedCell(XSSFName namedRange, XSSFSheet sheet, Double cotacaoVenda) {
        try {
            // Obtém a referência da célula nomeada
            String cellRef = namedRange.getRefersToFormula();
            System.out.println("Referência da célula nomeada: " + cellRef);
            
            // Remove o nome da planilha se estiver presente (formato: 'Sheet1'!A1)
            if (cellRef.contains("!")) {
                cellRef = cellRef.substring(cellRef.indexOf("!") + 1);
            }
            
            // Remove aspas se estiverem presentes
            cellRef = cellRef.replace("'", "").replace("$", "");
            System.out.println("Referência limpa: " + cellRef);
            
            // Extrai linha e coluna da referência
            int col = 0;
            int i = 0;
            // Calcula a coluna para suportar múltiplas letras (A, B, ..., Z, AA, AB, etc.)
            while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) {
                col = col * 26 + (cellRef.charAt(i) - 'A' + 1);
                i++;
            }
            col--; // Converte para índice zero-based
            
            int row = Integer.parseInt(cellRef.substring(i)) - 1; // Converte para índice zero-based
            
            System.out.println("Posição calculada - Linha: " + row + ", Coluna: " + col);
            
            // Define o valor na célula
            XSSFRow targetRow = sheet.getRow(row);
            if (targetRow == null) {
                targetRow = sheet.createRow(row);
                System.out.println("Criada nova linha: " + row);
            }
            XSSFCell dolarCell = targetRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            dolarCell.setCellValue(cotacaoVenda);
            System.out.println("Valor " + cotacaoVenda + " definido na célula " + cellRef);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar célula nomeada DOLAR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void updateCellByContent(XSSFSheet sheet, Double cotacaoVenda) {
        try {
            System.out.println("Procurando por células que contenham 'DOLAR'...");
            int totalRows = sheet.getLastRowNum();
            System.out.println("Total de linhas na planilha: " + (totalRows + 1));
            
            // Procura por célula que contenha "DOLAR" no texto
            for (int rowIndex = 0; rowIndex <= totalRows; rowIndex++) {
                XSSFRow row = sheet.getRow(rowIndex);
                if (row != null) {
                    int lastCol = row.getLastCellNum();
                    for (int colIndex = 0; colIndex < lastCol; colIndex++) {
                        XSSFCell cell = row.getCell(colIndex);
                        if (cell != null && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            String cellValue = cell.getStringCellValue();
                            if (cellValue != null && cellValue.toUpperCase().contains("DOLAR")) {
                                System.out.println("Encontrou célula com DOLAR na linha " + rowIndex + ", coluna " + colIndex + ": '" + cellValue + "'");
                                // Encontrou célula com "DOLAR", substitui o valor pela cotação
                                cell.setCellValue(cotacaoVenda);
                                System.out.println("Valor " + cotacaoVenda + " definido na célula com conteúdo DOLAR");
                                return; // Para após encontrar a primeira ocorrência
                            }
                        } else if (cell != null && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            // Verifica se pode ser uma célula que deve conter o valor do dólar
                            double numericValue = cell.getNumericCellValue();
                            if (numericValue == 5.50) {
                                System.out.println("Encontrou célula com valor 5.50 na linha " + rowIndex + ", coluna " + colIndex + " - pode ser o dólar fixo");
                                cell.setCellValue(cotacaoVenda);
                                System.out.println("Valor " + cotacaoVenda + " definido na célula que tinha 5.50");
                                return;
                            }
                        }
                    }
                }
            }
            System.out.println("Nenhuma célula com 'DOLAR' ou valor 5.50 foi encontrada");
        } catch (Exception e) {
            System.err.println("Erro ao procurar célula por conteúdo DOLAR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BigDecimal buscarCotacaoDolarAtual() {
        try {
            String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            CotacaoDolar cotacao = cotacaoDolarService.getCotacaoDolar(dataAtual);
            
            if (cotacao != null && !cotacao.getValue().isEmpty()) {
                Double cotacaoVenda = cotacao.getValue().get(0).getCotacaoVenda();
                if (cotacaoVenda != null) {
                    return BigDecimal.valueOf(cotacaoVenda);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar cotação automática do dólar: " + e.getMessage());
        }
        return null;
    }
}
