# API de Licitação

API REST para consulta de cotações do dólar e busca de fornecedores.

## Endpoints

### 1. Consulta de Cotação do Dólar
- **URL**: `/api/cotacao/dolar`
- **Método**: GET
- **Parâmetros**:
  - `data`: Data da cotação no formato dd-MM-yyyy
- **Exemplo**:
  ```
  GET /api/cotacao/dolar?data=01-06-2025
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

## Tecnologias Utilizadas
- Java 17
- Spring Boot 3.2.3
- Maven
- Lombok

## Como Executar
1. Clone o repositório
2. Execute `mvn spring-boot:run`
3. A API estará disponível em `http://localhost:8080` 