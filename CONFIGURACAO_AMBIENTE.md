# 🔧 Configuração de Variáveis de Ambiente

## 📋 Visão Geral

Este projeto utiliza variáveis de ambiente para proteger credenciais sensíveis e permitir configurações flexíveis entre diferentes ambientes (desenvolvimento, teste, produção).

## 🚀 Configuração Inicial

### 1. **Copiar Arquivo de Exemplo**

```bash
# Copie o arquivo de exemplo
cp environment.example environment.local
```

### 2. **Configurar Credenciais**

Edite o arquivo `environment.local` com suas credenciais reais:

```bash
# Abrir no editor de sua preferência
notepad environment.local      # Windows
nano environment.local         # Linux/Mac
code environment.local         # VS Code
```

### 3. **Configurar Credenciais Azure**

No arquivo `environment.local`, configure:

```bash
# Azure Blob Storage
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=SEU_ACCOUNT;AccountKey=SUA_KEY;EndpointSuffix=core.windows.net
AZURE_STORAGE_CONTAINER_NAME=seu_container

# Serviço de IA na Nuvem
CLOUD_PDF_SERVICE_URL=https://seu-servico.azurewebsites.net/score
```

## 🏃‍♂️ Executando a Aplicação

### **Windows**

```cmd
# Carregar variáveis e executar
load-env.bat
mvn spring-boot:run
```

### **Linux/Mac**

```bash
# Carregar variáveis e executar
source load-env.sh
mvn spring-boot:run
```

### **IntelliJ IDEA**

1. Abra **Run Configuration**
2. Vá em **Environment Variables**
3. Clique **Load from file**
4. Selecione `environment.local`

### **VS Code**

1. Instale extensão **DotENV**
2. O arquivo será carregado automaticamente

## 🔐 Variáveis Disponíveis

### **Obrigatórias**

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `AZURE_STORAGE_CONNECTION_STRING` | Connection string do Azure Blob Storage | `DefaultEndpointsProtocol=https;AccountName=...` |
| `CLOUD_PDF_SERVICE_URL` | URL do serviço de IA na nuvem | `https://app.azurewebsites.net/score` |

### **Opcionais (com valores padrão)**

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SERVER_PORT` | `1234` | Porta do servidor |
| `AZURE_STORAGE_CONTAINER_NAME` | `editals` | Nome do container Azure |
| `CLOUD_PDF_SERVICE_ENABLED` | `true` | Habilitar serviço IA |
| `MAX_FILE_SIZE` | `10MB` | Tamanho máximo de arquivo |
| `MAX_REQUEST_SIZE` | `10MB` | Tamanho máximo de request |

## 🚨 Segurança

### **⚠️ IMPORTANTE**

- **NUNCA** faça commit do arquivo `environment.local`
- **NUNCA** compartilhe credenciais em chats ou emails
- **SEMPRE** use variáveis de ambiente em produção

### **Arquivos Protegidos pelo .gitignore**

```
.env
.env.local
.env.production
environment
environment.local
environment.production
```

## 🌍 Ambientes

### **Desenvolvimento Local**
```bash
environment.local      # Suas credenciais locais
```

### **Produção**
```bash
# Definir via sistema operacional
export AZURE_STORAGE_CONNECTION_STRING="..."
export CLOUD_PDF_SERVICE_URL="..."

# Ou via Docker
docker run -e AZURE_STORAGE_CONNECTION_STRING="..." app
```

### **Docker Compose**
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - AZURE_STORAGE_CONNECTION_STRING=${AZURE_CONNECTION_STRING}
      - CLOUD_PDF_SERVICE_URL=${CLOUD_SERVICE_URL}
    env_file:
      - environment.local
```

## 🔍 Verificação

### **Testar Configuração**

```bash
# Windows
load-env.bat --keep-open

# Linux/Mac
source load-env.sh
echo $AZURE_STORAGE_CONNECTION_STRING
```

### **Verificar via API**

```bash
curl http://localhost:1234/api/cloud-pdf/status
```

**Resposta esperada:**
```json
{
  "serviceEnabled": true,
  "connectionOk": true,
  "status": "ONLINE"
}
```

## ❓ Troubleshooting

### **Problema: Variáveis não carregadas**

```bash
# Verificar se arquivo existe
ls -la environment.local

# Verificar conteúdo
cat environment.local
```

### **Problema: Azure não conecta**

```bash
# Verificar connection string
echo $AZURE_STORAGE_CONNECTION_STRING

# Testar via Azure CLI
az storage container list --connection-string "$AZURE_STORAGE_CONNECTION_STRING"
```

### **Problema: Serviço IA indisponível**

```bash
# Verificar URL
curl $CLOUD_PDF_SERVICE_URL

# Verificar logs da aplicação
tail -f logs/application.log
```

## 📚 Referências

- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Azure Blob Storage Connection Strings](https://docs.microsoft.com/en-us/azure/storage/common/storage-configure-connection-string)
- [12 Factor App - Config](https://12factor.net/config)

---

## ✅ Checklist de Configuração

- [ ] Arquivo `environment.local` criado
- [ ] Credenciais Azure configuradas
- [ ] URL do serviço IA configurada
- [ ] Variáveis carregadas com sucesso
- [ ] Aplicação iniciada sem erros
- [ ] API de status retornando "ONLINE"
- [ ] Upload de PDF funcionando
- [ ] Download de Excel funcionando

**Tudo configurado corretamente!** 🎉 