#!/bin/bash

# ==============================================
# SCRIPT PARA CARREGAR VARIÁVEIS DE AMBIENTE
# ==============================================
# Execute: source load-env.sh

echo "Carregando variáveis de ambiente..."

# Verificar se arquivo de environment existe
if [ ! -f "environment.local" ]; then
    echo "ERRO: Arquivo environment.local não encontrado!"
    echo "Copie environment.example para environment.local e configure as credenciais"
    exit 1
fi

echo "Lendo arquivo environment.local..."

# Carregar variáveis do arquivo
while IFS='=' read -r key value; do
    # Ignorar comentários e linhas vazias
    if [[ ! $key =~ ^#.* ]] && [[ -n $key ]] && [[ -n $value ]]; then
        export "$key"="$value"
        echo "Variável carregada: $key"
    fi
done < environment.local

echo ""
echo "========================================"
echo "VARIÁVEIS DE AMBIENTE CARREGADAS:"
echo "========================================"
echo "SERVER_PORT=$SERVER_PORT"
echo "AZURE_STORAGE_CONTAINER_NAME=$AZURE_STORAGE_CONTAINER_NAME"
echo "CLOUD_PDF_SERVICE_ENABLED=$CLOUD_PDF_SERVICE_ENABLED"
echo "MAX_FILE_SIZE=$MAX_FILE_SIZE"
echo "MAX_REQUEST_SIZE=$MAX_REQUEST_SIZE"
echo "========================================"
echo ""

echo "Variáveis carregadas com sucesso!"
echo "Para iniciar a aplicação, execute: mvn spring-boot:run" 