package com.api.licitacao.service;

import com.api.licitacao.dto.ResultadoDTO;
import com.api.licitacao.dto.ItemDTO;
import com.api.licitacao.exception.ExcelGenerationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResultadoServiceImpl implements ResultadoService {

    private static final String TEMPLATE_PATH = "templates/resultado.xlsx";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");

    @Override
    public byte[] generateResultado(ResultadoDTO dto) {
        try (InputStream is = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            fillHeader(sheet, dto);
            fillItems(sheet, dto.items());

            workbook.write(os);
            return os.toByteArray();

        } catch (IOException ex) {
            throw new ExcelGenerationException("Erro ao gerar planilha de resultado", ex);
        }
    }

    private void fillHeader(XSSFSheet sheet, ResultadoDTO dto) {
        // Linha 1 (índice 0): PROC., ORGÃO, DATA
        sheet.getRow(0).getCell(0)
             .setCellValue("PROC. " + dto.processNumber());
        sheet.getRow(0).getCell(2)
             .setCellValue("ORGÃO: " + dto.organ());
        sheet.getRow(0).getCell(7)
             .setCellValue("DATA: " + dto.date().format(DATE_FMT));
    }

    private void fillItems(XSSFSheet sheet, List<ItemDTO> items) {
        final int TEMPLATE_DATA_ROW = 2;    // índice da primeira linha de item no template
        final int TEMPLATE_FORMULA_ROW = 3; // índice da linha de fórmulas logo abaixo

        for (int i = 0; i < items.size(); i++) {
            int dataRowIdx = TEMPLATE_DATA_ROW + i * 2;
            int formulaRowIdx = TEMPLATE_FORMULA_ROW + i * 2;

            // Nas iterações além da primeira, clonamos as duas linhas do template
            if (i > 0) {
                cloneRow(sheet, TEMPLATE_DATA_ROW, dataRowIdx);
                cloneRow(sheet, TEMPLATE_FORMULA_ROW, formulaRowIdx);
            }

            ItemDTO item = items.get(i);
            XSSFRow dataRow = sheet.getRow(dataRowIdx);
            setCellValue(dataRow, 0, item.item());
            setCellValue(dataRow, 1, item.product());
            setCellValue(dataRow, 2, item.quantity());
            setCellValue(dataRow, 3, item.position());
            setCellValue(dataRow, 4, item.empresa());
            setCellValue(dataRow, 5, item.marcaMod());
            setCellValue(dataRow, 6, item.custo().doubleValue());
            setCellValue(dataRow, 7, item.valores().doubleValue());

            // Preenche as fórmulas dinamicamente, mantendo estilo original
            XSSFRow fRow = sheet.getRow(formulaRowIdx);
            XSSFCell cValor     = fRow.getCell(7);
            XSSFCell cEstimado  = fRow.getCell(8);

            String rowRef = String.valueOf(dataRowIdx + 1);
            cValor.setCellFormula("H" + rowRef + "*C" + rowRef);
            cEstimado.setCellFormula("I" + rowRef + "*C" + rowRef);
        }
    }

    private void setCellValue(XSSFRow row, int col, String value) {
        XSSFCell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
    }

    private void setCellValue(XSSFRow row, int col, double value) {
        XSSFCell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
    }

    private void cloneRow(XSSFSheet sheet, int srcRowNum, int destRowNum) {
        sheet.shiftRows(destRowNum, sheet.getLastRowNum(), 1);
        XSSFRow srcRow = sheet.getRow(srcRowNum);
        XSSFRow newRow = sheet.createRow(destRowNum);
        newRow.setHeight(srcRow.getHeight());

        for (int c = srcRow.getFirstCellNum(); c < srcRow.getLastCellNum(); c++) {
            XSSFCell oldCell = srcRow.getCell(c);
            if (oldCell == null) continue;
            XSSFCell newCell = newRow.createCell(c);
            newCell.setCellStyle(oldCell.getCellStyle());
            switch (oldCell.getCellType()) {
                case STRING   -> newCell.setCellValue(oldCell.getStringCellValue());
                case NUMERIC  -> newCell.setCellValue(oldCell.getNumericCellValue());
                case BOOLEAN  -> newCell.setCellValue(oldCell.getBooleanCellValue());
                case FORMULA  -> newCell.setCellFormula(oldCell.getCellFormula());
                default       -> newCell.setBlank();
            }
        }
    }
}
