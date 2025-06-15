# Configuração do Azure Blob Storage

## Visão Geral

Esta implementação adiciona suporte para upload automático de arquivos PDF para o Azure Blob Storage nos endpoints de processamento de PDFs. A funcionalidade é **opcional** e não interfere na lógica existente da aplicação.

## Recursos Implementados

✅ Upload automático de PDFs para Azure Blob Storage  
✅ Geração de nomes únicos para evitar conflitos  
✅ Preservação do nome original do arquivo  
✅ Tratamento de erros sem interromper o processamento principal  
✅ Configuração opcional via properties  
✅ Logs detalhados para debugging  

## Configuração

### 1. Dependência Maven

A dependência já foi adicionada ao `pom.xml`:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.25.1</version>
</dependency>
```

### 2. Configurar Connection String

#### Opção A: Via application.properties
```properties
# Configuração do Azure Blob Storage
azure.storage.connection-string=DefaultEndpointsProtocol=https;AccountName=YOUR_ACCOUNT_NAME;AccountKey=YOUR_ACCOUNT_KEY;EndpointSuffix=core.windows.net
azure.storage.container-name=licitacao-pdfs
```

#### Opção B: Via Variáveis de Ambiente
```bash
export AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=YOUR_ACCOUNT_NAME;AccountKey=YOUR_ACCOUNT_KEY;EndpointSuffix=core.windows.net"
export AZURE_STORAGE_CONTAINER_NAME="licitacao-pdfs"
```

#### Opção C: Via application.yml
```yaml
azure:
  storage:
    connection-string: DefaultEndpointsProtocol=https;AccountName=YOUR_ACCOUNT_NAME;AccountKey=YOUR_ACCOUNT_KEY;EndpointSuffix=core.windows.net
    container-name: licitacao-pdfs
```

### 3. Obter Connection String do Azure

1. **Acesse o Portal Azure**: https://portal.azure.com
2. **Navegue para Storage Accounts**
3. **Selecione ou crie uma Storage Account**
4. **Vá para "Access keys"**
5. **Copie a Connection String**

## Como Funciona

### Fluxo de Upload

1. **Recebimento do PDF**: Endpoint recebe o arquivo PDF
2. **Upload para Azure**: Arquivo é enviado para Azure Blob Storage (se configurado)
3. **Processamento Normal**: Lógica existente continua inalterada
4. **Resposta**: Planilha Excel é retornada normalmente

### Nomenclatura de Arquivos

Arquivos são salvos com padrão único:
```
[nome_original]_[timestamp]_[uuid].[extensao]

Exemplo:
documento.pdf → documento_20240315_143022_a1b2c3d4.pdf
```

### Estrutura do Container

```
licitacao-pdfs/
├── documento_20240315_143022_a1b2c3d4.pdf
├── edital_20240315_143025_e5f6g7h8.pdf
└── licitacao_20240315_143030_i9j0k1l2.pdf
```

## Endpoints Afetados

### 1. Processar Múltiplos PDFs
**POST** `/api/processamento/processar-pdfs`

- Faz upload de todos os PDFs válidos
- Retorna lista de nomes de blobs no log
- Continua processamento mesmo com falhas no upload

### 2. Processar PDF Único
**POST** `/api/processamento/processar-pdf-unico`

- Faz upload do PDF individual
- Retorna nome do blob no log
- Continua processamento mesmo com falha no upload

## Logs e Monitoramento

### Logs de Sucesso
```
Iniciando upload do arquivo 'documento.pdf' como 'documento_20240315_143022_a1b2c3d4.pdf'
Upload do arquivo 'documento.pdf' concluído com sucesso. Blob: 'documento_20240315_143022_a1b2c3d4.pdf'
Arquivos enviados para Azure: [documento_20240315_143022_a1b2c3d4.pdf]
```

### Logs de Erro
```
Connection string do Azure não configurada. Upload ignorado.
Erro no upload para Azure (continuando processamento): [mensagem de erro]
Erro ao fazer upload do arquivo 'documento.pdf' para Azure: [detalhes do erro]
```

## Tratamento de Erros

A implementação garante que **falhas no upload não interrompam o processamento principal**:

1. **Azure não configurado**: Upload é ignorado silenciosamente
2. **Falha na conexão**: Erro é logado, processamento continua
3. **Arquivo inválido**: Erro é logado, outros arquivos são processados
4. **Quota excedida**: Erro é logado, processamento continua

## Segurança

### Boas Práticas

1. **Não commit de credentials**: Nunca commite connection strings no código
2. **Use variáveis de ambiente**: Especialmente em produção
3. **Rotação de chaves**: Rotacione access keys periodicamente
4. **Princípio do menor privilégio**: Configure apenas permissões necessárias

### Permissões Necessárias

A connection string deve ter permissões para:
- ✅ Criar containers
- ✅ Upload de blobs
- ✅ Definir metadados

## Troubleshooting

### Problema: "Connection string not configured"
**Solução**: Verificar se `azure.storage.connection-string` está definida

### Problema: "Container not found"
**Solução**: Container é criado automaticamente. Verificar permissões da connection string

### Problema: "Authentication failed"
**Solução**: Verificar se connection string está correta e account key é válida

### Problema: Upload lento
**Solução**: Considerar configurar timeout e retry policies customizadas

## Desabilitar Funcionalidade

Para desabilitar completamente o upload para Azure:

1. **Remover/comentar a connection string**:
```properties
# azure.storage.connection-string=
```

2. **Ou usar valor vazio**:
```properties
azure.storage.connection-string=
```

A aplicação continuará funcionando normalmente sem fazer uploads.

## Exemplo de Teste

```bash
# Testar endpoint com curl
curl -X POST http://localhost:8080/api/processamento/processar-pdf-unico \
  -F "arquivo=@/caminho/para/documento.pdf" \
  -H "Content-Type: multipart/form-data"
```

## Monitoramento no Azure

1. **Acesse Storage Account no Portal Azure**
2. **Vá para "Containers"**
3. **Selecione container "licitacao-pdfs"**
4. **Visualize arquivos uploadados**

## Custos

O upload para Azure Blob Storage tem custos baseados em:
- **Armazenamento**: ~$0.0184/GB por mês (Hot tier)
- **Transações**: ~$0.0004 por 10,000 write operations
- **Transferência**: Gratuita para upload

*Valores aproximados, consulte pricing do Azure para valores atuais* 