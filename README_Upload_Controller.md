# Upload Controller - Documentação

## Descrição
O `UploadController` foi criado para receber uma lista de documentos PDF e extrair dados estruturados usando o `CapaService` e `PdfReaderService`.

## Classes Criadas

### 1. UploadController
- **Localização**: `src/main/java/com/api/licitacao/controller/UploadController.java`
- **Endpoints**:
  - `POST /api/upload/documentos` - Processa múltiplos arquivos PDF
  - `POST /api/upload/documento` - Processa um único arquivo PDF

### 2. PdfReaderService  
- **Localização**: `src/main/java/com/api/licitacao/service/PdfReaderService.java`
- **Funcionalidade**: Extrai dados de PDFs usando expressões regulares (regex)
- **Tecnologia**: Apache PDFBox

### 3. CapaService (Atualizado)
- **Localização**: `src/main/java/com/api/licitacao/service/CapaService.java`
- **Método Adicionado**: `extrairDadosPdf(MultipartFile arquivo)`

## Uso dos Endpoints

### Upload de Múltiplos Documentos
```bash
curl -X POST "http://localhost:8080/api/upload/documentos" \
  -H "Content-Type: multipart/form-data" \
  -F "arquivos=@documento1.pdf" \
  -F "arquivos=@documento2.pdf"
```

### Upload de Documento Único
```bash
curl -X POST "http://localhost:8080/api/upload/documento" \
  -H "Content-Type: multipart/form-data" \
  -F "arquivo=@documento.pdf"
```

## Resposta (CapaDTO)
```json
{
  "processo": "123456/2024",
  "dataHora": "2024-06-11T15:30:00",
  "organ": "Prefeitura Municipal",
  "headerTitle": "Processo de Licitação",
  "portal": "Portal de Compras",
  "edital": "001/2024",
  "cliente": "Empresa XYZ",
  "objeto": "Aquisição de materiais",
  "modalidade": "Pregão Eletrônico",
  "amostra": "Necessária",
  "entrega": "30 dias",
  "cr": "001.001",
  "atestado": true,
  "impugnacao": "Não há",
  "obs": "Observações gerais",
  "cotacaoDolar": null,
  "items": null
}
```

## Padrões de Extração de Dados
O `PdfReaderService` utiliza os seguintes padrões regex para extrair informações:

- **Processo**: `processo[:\-\s]*([\\w\\d\\-\\/\\.]+)`
- **Órgão**: `(?:órgão|orgao)[:\-\s]*([^\\n\\r]+)`
- **Título**: `(?:título|titulo|objeto)[:\-\s]*([^\\n\\r]+)`
- **Portal**: `portal[:\-\s]*([^\\n\\r]+)`
- **Edital**: `edital[:\-\s]*([\\w\\d\\-\\/\\.]+)`
- **Cliente**: `cliente[:\-\s]*([^\\n\\r]+)`
- **Objeto**: `objeto[:\-\s]*([^\\n\\r]+)`
- **Modalidade**: `modalidade[:\-\s]*([^\\n\\r]+)`
- **Amostra**: `amostra[:\-\s]*([^\\n\\r]+)`
- **Entrega**: `(?:entrega|prazo)[:\-\s]*([^\\n\\r]+)`
- **CR**: `(?:cr|centro de responsabilidade)[:\-\s]*([^\\n\\r]+)`
- **Atestado**: `atestado[:\-\s]*(sim|não|yes|no|s|n)`
- **Impugnação**: `(?:impugnação|impugnacao)[:\-\s]*([^\\n\\r]+)`
- **Observações**: `(?:observações|observacoes|obs)[:\-\s]*([^\\n\\r]+)`

## Validações
- Verifica se o arquivo é realmente um PDF (content-type e extensão)
- Trata arquivos vazios
- Retorna erros HTTP apropriados (400, 500)

## Exemplo de Integração
```java
@Autowired
private CapaService capaService;

// Extrair dados de um PDF
CapaDTO dados = capaService.extrairDadosPdf(arquivo);

// Gerar Excel com os dados extraídos
byte[] excel = capaService.generateCapa(dados);
``` 