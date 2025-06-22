# API de Licitação

API REST para consulta de cotações do dólar, busca de fornecedores e processamento de documentos PDF com geração de planilhas Excel.

## Endpoints

### 1. Consulta de Cotação do Dólar
- **URL**: `/api/cotacao/dolar`
- **Método**: GET
- **Parâmetros**: Nenhum
- **Exemplo**:
  ```
  GET /api/cotacao/dolar
  ```
- **Resposta**:
  ```json
  {
    "moeda": "USD",
    "tipo": "Dólar Americano/Real Brasileiro",
    "cotacao": "5.5181",
    "dataCotacao": "2025-06-20 18:20:32"
  }
  ```

### 2. Busca de Fornecedores
- **URL**: `/api/fornecedores/buscar`
- **Método**: POST
- **Corpo da Requisição**:
  ```json
  {
    "descricaoDetalhada": "Descrição do produto...",
    "tratamentoDiferenciado": "Não",
    "aplicabilidadeDecreto": "Não",
    "quantidadeTotal": 500,
    "criterioJulgamento": "Menor Preço",
    "criterioValor": "Valor Estimado",
    "valorTotal": 438165.00,
    "unidadeFornecimento": "Unidade",
    "intervaloMinimoLances": 100.00
  }
  ```

### 3. Processamento de PDFs
#### 3.1 Processar Múltiplos PDFs
- **URL**: `/api/processamento/processar-pdfs`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivos` (List<MultipartFile>)
- **Retorna**: Planilha Excel com dados processados

#### 3.2 Processar PDF Único
- **URL**: `/api/processamento/processar-pdf-unico`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivo` (MultipartFile)
- **Retorna**: Planilha Excel com dados processados

### 4. Upload de Documentos
#### 4.1 Upload Múltiplos Documentos
- **URL**: `/api/upload/documentos`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivos` (List<MultipartFile>)

#### 4.2 Upload Documento Único
- **URL**: `/api/upload/documento`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivo` (MultipartFile)

### 5. Processamento na Nuvem
#### 5.1 Status do Serviço
- **URL**: `/api/cloud/status`
- **Método**: GET

#### 5.2 Processar PDF na Nuvem
- **URL**: `/api/cloud/processar`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivo` (MultipartFile)

### 6. Geração de Planilhas Excel
#### 6.1 Gerar Planilha de Capa
- **URL**: `/api/excel/capa`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivo` (MultipartFile)

#### 6.2 Gerar Planilha de Itens
- **URL**: `/api/excel/itens`
- **Método**: POST
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `arquivo` (MultipartFile)

## Fornecedores Disponíveis

### Fornecedores Nacionais (Preços em R$)
1. **Mundo da Carabina** - Lanternas táticas
2. **Falcon Armas** - Equipamentos táticos e de aventura
3. **Casa da Pesca** - Lanternas de alta performance

### Fornecedores Importados (Preços em USD)
1. **Tactical Gear USA** - Equipamentos táticos militares

## Estrutura da Planilha

A planilha gerada inclui as seguintes colunas:
- **Item**: Número sequencial
- **Tipo**: Tipo do produto
- **Descrição**: Descrição detalhada com fornecedor
- **Quantidade**: Quantidade solicitada
- **Custo Unitário**: Preço unitário (R$ ou USD)
- **Frete**: Custo de frete (R$ ou USD)
- **Nacional**: "N" para nacional, vazio para importado

## Tecnologias Utilizadas
- Java 17
- Spring Boot 3.2.3
- Maven
- Lombok
- Apache POI (Excel)
- Azure Blob Storage

## Como Executar
1. Clone o repositório
2. Execute `mvn spring-boot:run`
3. A API estará disponível em `http://localhost:1234` 