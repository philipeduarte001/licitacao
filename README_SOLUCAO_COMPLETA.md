# Sistema de Processamento Inteligente de PDFs com IA e Azure

## 📋 Visão Geral

Este sistema oferece uma solução completa para processamento inteligente de documentos PDF de licitações, integrando upload via frontend, armazenamento em nuvem Azure, processamento via IA e geração automatizada de planilhas Excel.

## 🎯 Funcionalidades Principais

- **📤 Upload de PDFs**: Interface para envio de documentos únicos ou múltiplos
- **☁️ Armazenamento Azure**: Backup automático no Azure Blob Storage
- **🤖 Processamento IA**: Extração inteligente de dados via serviço na nuvem
- **📊 Planilha Excel**: Geração automática de planilhas formatadas
- **🔄 Fallback Local**: Processamento via regex como backup
- **📈 Cotação Dólar**: Integração com API do Banco Central
- **🔍 Busca Fornecedores**: Sistema de recomendação de fornecedores

## 🏗️ Arquitetura da Solução

```
[Frontend] → [API Controller] → [Azure Blob Storage]
                ↓
         [Serviço IA na Nuvem] → [CapaDTO]
                ↓                    ↓
         [Busca Fornecedores] → [Geração Excel] → [Download]
                ↓
         [Cotação Dólar]
```

## 🚀 Fluxo Completo do Sistema

### 1. **Upload do PDF** 📤
- Cliente faz upload via frontend ou API
- Validação de formato (PDF obrigatório)
- Suporte a múltiplos arquivos

### 2. **Armazenamento Azure** ☁️
- Upload automático para Azure Blob Storage
- Nomenclatura única: `{nome}_{timestamp}_{uuid}.pdf`
- Container configurável (padrão: `editals`)

### 3. **Processamento IA** 🤖
- Envio para serviço de IA na nuvem
- Extração inteligente de dados estruturados
- Parse de JSON complexo com validação

### 4. **Enriquecimento de Dados** 📊
- Busca automática de fornecedores relacionados
- Obtenção de cotação do dólar atualizada
- Geração de itens com preços simulados

### 5. **Geração Excel** 📈
- Criação de planilha formatada
- Preenchimento automático de dados
- Fórmulas e formatação preservadas

### 6. **Download** ⬇️
- Resposta HTTP com arquivo Excel
- Headers configurados para download automático
- Nome personalizado do arquivo

## ⚙️ Configuração do Sistema

### 1. **Dependências Maven**

Adicione no `pom.xml`:

```xml
<!-- Azure Blob Storage -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.25.1</version>
</dependency>

<!-- PDF Processing -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>

<!-- Excel Processing -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 2. **Configuração de Variáveis de Ambiente**

⚠️ **IMPORTANTE**: As credenciais foram movidas para variáveis de ambiente por segurança.

#### **Configuração Inicial**

```bash
# 1. Copie o arquivo de exemplo
cp environment.example environment.local

# 2. Configure suas credenciais reais no environment.local
# (Este arquivo está no .gitignore e não será commitado)

# 3. Carregue as variáveis
# Windows
load-env.bat

# Linux/Mac
source load-env.sh
```

#### **Arquivo application.properties (sem credenciais)**

```properties
# Servidor
server.port=${SERVER_PORT:1234}

# Azure Blob Storage (via variáveis de ambiente)
azure.storage.connection-string=${AZURE_STORAGE_CONNECTION_STRING:}
azure.storage.container-name=${AZURE_STORAGE_CONTAINER_NAME:editals}

# Serviço de Processamento PDF na Nuvem (via variáveis de ambiente)
cloud.pdf.service.url=${CLOUD_PDF_SERVICE_URL:}
cloud.pdf.service.enabled=${CLOUD_PDF_SERVICE_ENABLED:true}

# Upload de arquivos
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:10MB}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE:10MB}

# Database H2 (para fornecedores)
spring.datasource.url=jdbc:h2:mem:licitacaodb
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# Swagger
springdoc.swagger-ui.path=/swagger
```

### 3. **Variáveis de Ambiente Necessárias**

```bash
# OBRIGATÓRIAS
AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net"
CLOUD_PDF_SERVICE_URL="https://seu-servico.azurewebsites.net/score"

# OPCIONAIS (têm valores padrão)
SERVER_PORT=1234
AZURE_STORAGE_CONTAINER_NAME=editals
CLOUD_PDF_SERVICE_ENABLED=true
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=10MB
```

## 📡 Endpoints da API

### **Processamento Completo**

#### 1. **Processar PDF Único**
```http
POST /api/processamento/processar-pdf-unico
Content-Type: multipart/form-data

arquivo: [arquivo.pdf]
```

**Resposta**: Arquivo Excel para download

#### 2. **Processar Múltiplos PDFs**
```http
POST /api/processamento/processar-pdfs
Content-Type: multipart/form-data

arquivos: [arquivo1.pdf, arquivo2.pdf, ...]
```

**Resposta**: Arquivo Excel consolidado

### **Testes do Serviço IA**

#### 3. **Status do Serviço**
```http
GET /api/cloud-pdf/status
```

**Resposta**:
```json
{
  "serviceEnabled": true,
  "connectionOk": true,
  "status": "ONLINE"
}
```

#### 4. **Testar Processamento**
```http
POST /api/cloud-pdf/processar?fileName=edital.pdf
```

#### 5. **Testar Parse JSON**
```http
POST /api/cloud-pdf/teste-json-response
```

## 💻 Integração Frontend

### **HTML + JavaScript**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Upload PDF - Sistema de Licitações</title>
    <style>
        .upload-area {
            border: 2px dashed #ccc;
            padding: 20px;
            text-align: center;
            margin: 20px 0;
        }
        .loading { display: none; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>
    <h1>Sistema de Processamento de PDFs</h1>
    
    <!-- Upload Único -->
    <div class="upload-section">
        <h2>Upload de PDF Único</h2>
        <div class="upload-area">
            <input type="file" id="singleFile" accept=".pdf" />
            <br><br>
            <button onclick="uploadSingle()">Processar PDF</button>
        </div>
    </div>

    <!-- Upload Múltiplo -->
    <div class="upload-section">
        <h2>Upload de Múltiplos PDFs</h2>
        <div class="upload-area">
            <input type="file" id="multipleFiles" accept=".pdf" multiple />
            <br><br>
            <button onclick="uploadMultiple()">Processar PDFs</button>
        </div>
    </div>

    <!-- Status -->
    <div id="status"></div>
    <div id="loading" class="loading">Processando...</div>

    <script>
        const API_BASE = 'http://localhost:1234/api/processamento';

        async function uploadSingle() {
            const fileInput = document.getElementById('singleFile');
            const file = fileInput.files[0];
            
            if (!file) {
                showStatus('Selecione um arquivo PDF', 'error');
                return;
            }

            const formData = new FormData();
            formData.append('arquivo', file);

            try {
                showLoading(true);
                const response = await fetch(`${API_BASE}/processar-pdf-unico`, {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const blob = await response.blob();
                    downloadFile(blob, `capa_${file.name.replace('.pdf', '')}.xlsx`);
                    showStatus('PDF processado com sucesso!', 'success');
                } else {
                    showStatus('Erro ao processar PDF', 'error');
                }
            } catch (error) {
                showStatus(`Erro: ${error.message}`, 'error');
            } finally {
                showLoading(false);
            }
        }

        async function uploadMultiple() {
            const fileInput = document.getElementById('multipleFiles');
            const files = fileInput.files;
            
            if (files.length === 0) {
                showStatus('Selecione pelo menos um arquivo PDF', 'error');
                return;
            }

            const formData = new FormData();
            for (let file of files) {
                formData.append('arquivos', file);
            }

            try {
                showLoading(true);
                const response = await fetch(`${API_BASE}/processar-pdfs`, {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const blob = await response.blob();
                    downloadFile(blob, 'capa_processada.xlsx');
                    showStatus(`${files.length} PDFs processados com sucesso!`, 'success');
                } else {
                    showStatus('Erro ao processar PDFs', 'error');
                }
            } catch (error) {
                showStatus(`Erro: ${error.message}`, 'error');
            } finally {
                showLoading(false);
            }
        }

        function downloadFile(blob, filename) {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        }

        function showStatus(message, type) {
            const statusDiv = document.getElementById('status');
            statusDiv.textContent = message;
            statusDiv.className = type;
        }

        function showLoading(show) {
            document.getElementById('loading').style.display = show ? 'block' : 'none';
        }

        // Verificar status do serviço ao carregar a página
        async function checkServiceStatus() {
            try {
                const response = await fetch('http://localhost:1234/api/cloud-pdf/status');
                const status = await response.json();
                console.log('Status do serviço:', status);
            } catch (error) {
                console.warn('Não foi possível verificar status do serviço');
            }
        }

        window.onload = checkServiceStatus;
    </script>
</body>
</html>
```

## 🧪 Testes da Solução

### **1. Teste Completo via cURL**

```bash
# Upload único
curl -X POST http://localhost:1234/api/processamento/processar-pdf-unico \
  -F "arquivo=@edital.pdf" \
  -H "Content-Type: multipart/form-data" \
  --output resultado.xlsx

# Upload múltiplo
curl -X POST http://localhost:1234/api/processamento/processar-pdfs \
  -F "arquivos=@edital1.pdf" \
  -F "arquivos=@edital2.pdf" \
  -H "Content-Type: multipart/form-data" \
  --output resultado_multiplo.xlsx

# Verificar status do serviço IA
curl -X GET http://localhost:1234/api/cloud-pdf/status

# Testar parse JSON
curl -X POST http://localhost:1234/api/cloud-pdf/teste-json-response
```

### **2. Teste via Postman**

1. **Criar Nova Collection**: "Sistema Licitações"

2. **Request 1**: Upload PDF Único
   - **Method**: POST
   - **URL**: `http://localhost:1234/api/processamento/processar-pdf-unico`
   - **Body**: form-data
     - Key: `arquivo`, Type: File, Value: [selecionar PDF]

3. **Request 2**: Status Serviço
   - **Method**: GET
   - **URL**: `http://localhost:1234/api/cloud-pdf/status`

## 📊 Estrutura de Dados

### **JSON Retornado pelo Serviço IA**

```json
{
  "extracted_clausules": {
    "edital": [
      {
        "processo": "2024-C5D7D",
        "dataHora": "14/05/2025 11:24",
        "cliente": "Secretaria de Estado da Segurança Pública...",
        "objeto": "Aquisição de 500 unidades de Lanternas Táticas...",
        "cotacaoDolar": null,
        "items": [
          {
            "item": 1,
            "descricao": "Lanternas Táticas de Mão...",
            "quantidade": 500,
            "custoUnitario": 876.33,
            "frete": 0.0
          }
        ]
      }
    ]
  }
}
```

## 🔧 Troubleshooting

### **Problemas Comuns**

#### 1. **Erro de Conexão Azure**
```
Erro: "Connection string not configured"
```
**Solução**: Verificar `azure.storage.connection-string` no application.properties

#### 2. **Serviço IA Indisponível**
```
Logs: "Usando fallback para processamento local"
```
**Solução**: Sistema usa regex como backup automaticamente

#### 3. **Upload Muito Grande**
```
Erro: "Maximum upload size exceeded"
```
**Solução**: Ajustar `spring.servlet.multipart.max-file-size`

### **Logs Importantes**

```bash
# Sucesso completo
INFO  - Tentando processar PDF 'edital.pdf' via serviço na nuvem
INFO  - PDF 'edital.pdf' enviado para Azure como 'edital_20241215_143022_a1b2c3d4.pdf'
INFO  - Processamento via serviço na nuvem bem-sucedido para 'edital.pdf'

# Fallback local
WARN  - Resultado do serviço na nuvem inválido, usando fallback local
INFO  - Processando PDF 'edital.pdf' via método local (regex)
```

## 🚀 Deploy e Produção

### **Configuração Docker**

```dockerfile
FROM openjdk:21-jdk-slim

COPY target/licitacao-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1234

ENV AZURE_STORAGE_CONNECTION_STRING=""
ENV CLOUD_PDF_SERVICE_URL="https://app-cbe-ultramar-dev-azb9fnfvandvg7dx.brazilsouth-01.azurewebsites.net/score"

ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📈 Monitoramento

### **Health Checks**

```bash
# Status geral da aplicação
curl http://localhost:1234/actuator/health

# Status do serviço IA
curl http://localhost:1234/api/cloud-pdf/status

# Swagger documentation
curl http://localhost:1234/swagger
```

## 🔐 Segurança

### **🛡️ Proteção de Credenciais**

Este projeto foi configurado com as melhores práticas de segurança:

#### **Variáveis de Ambiente Protegidas**

```bash
# ✅ SEGURO - Arquivos protegidos pelo .gitignore
environment.local          # Suas credenciais reais (não commitado)
environment.example        # Exemplo sem credenciais (commitado)

# ❌ EVITADO - Credenciais hardcoded no código
# azure.storage.connection-string=DefaultEndpointsProtocol=https;AccountName=...
```

#### **Estrutura de Arquivos de Security**

```
projeto/
├── environment.example          # ✅ Template (commitado)
├── environment.local           # ❌ Credenciais reais (ignorado)
├── load-env.bat               # ✅ Script Windows (commitado)
├── load-env.sh                # ✅ Script Linux/Mac (commitado)
├── .gitignore                 # ✅ Proteções (commitado)
└── SETUP_AMBIENTE.md          # ✅ Documentação (commitado)
```

### **Boas Práticas Implementadas**

1. **✅ Credentials**: Variáveis de ambiente obrigatórias
2. **✅ File Validation**: Validação de tipo e tamanho de arquivos
3. **✅ Gitignore**: Proteção de arquivos sensíveis
4. **✅ Documentation**: Guias de configuração segura
5. **✅ Scripts**: Automação de carregamento de variáveis
6. **✅ Environment Separation**: Diferentes configs por ambiente

### **🚨 Alertas de Segurança**

#### **NUNCA faça commit de:**
- Arquivos `environment.local`
- Arquivos `.env` com credenciais
- Connection strings no código
- Chaves de API hardcoded

#### **SEMPRE use:**
- Variáveis de ambiente para credenciais
- Arquivos `.example` como templates
- `.gitignore` para proteger arquivos sensíveis

## 📚 Documentação Adicional

- **Swagger UI**: `http://localhost:1234/swagger`
- **API Docs**: `http://localhost:1234/api-docs`
- **H2 Console**: `http://localhost:1234/h2-console`

## 🆘 FAQ

**Q: O sistema funciona sem Azure?**
**A**: Sim, o upload para Azure é opcional. O sistema funciona normalmente sem configuração.

**Q: O que acontece se o serviço IA estiver offline?**
**A**: O sistema automaticamente usa processamento local via regex como fallback.

**Q: Posso processar outros formatos além de PDF?**
**A**: Atualmente suporta apenas PDF. Para outros formatos, seria necessário extensão do código.

---

## 🎉 Conclusão

Este sistema oferece uma solução robusta e escalável para processamento inteligente de documentos PDF, combinando IA moderna com fallbacks confiáveis, integração em nuvem e interface amigável.

**Sistema pronto para produção!** 🚀 