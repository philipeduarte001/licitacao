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

## 🔐 Variáveis Disponíveis

### **Obrigatórias**

| Variável | Descrição |
|----------|-----------|
| `AZURE_STORAGE_CONNECTION_STRING` | Connection string do Azure Blob Storage |
| `CLOUD_PDF_SERVICE_URL` | URL do serviço de IA na nuvem |

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

## ✅ Checklist de Configuração

- [ ] Arquivo `environment.local` criado
- [ ] Credenciais Azure configuradas
- [ ] URL do serviço IA configurada
- [ ] Variáveis carregadas com sucesso
- [ ] Aplicação iniciada sem erros
- [ ] API de status retornando "ONLINE"

**Tudo configurado corretamente!** 🎉 