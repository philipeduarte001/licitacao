@echo off
REM ==============================================
REM SCRIPT PARA CARREGAR VARIÁVEIS DE AMBIENTE
REM ==============================================
REM Execute este script antes de iniciar a aplicação

echo Carregando variáveis de ambiente...

REM Verificar se arquivo de environment existe
if not exist "environment.local" (
    echo ERRO: Arquivo environment.local nao encontrado!
    echo Copie environment.example para environment.local e configure as credenciais
    pause
    exit /b 1
)

echo Lendo arquivo environment.local...

REM Carregar variáveis do arquivo
for /f "usebackq tokens=1,2 delims==" %%i in ("environment.local") do (
    REM Ignorar comentários e linhas vazias
    echo %%i | findstr /r "^#" >nul
    if errorlevel 1 (
        if not "%%i"=="" (
            if not "%%j"=="" (
                set "%%i=%%j"
                echo Variável carregada: %%i
            )
        )
    )
)

echo.
echo ========================================
echo VARIAVEIS DE AMBIENTE CARREGADAS:
echo ========================================
echo SERVER_PORT=%SERVER_PORT%
echo AZURE_STORAGE_CONTAINER_NAME=%AZURE_STORAGE_CONTAINER_NAME%
echo CLOUD_PDF_SERVICE_ENABLED=%CLOUD_PDF_SERVICE_ENABLED%
echo MAX_FILE_SIZE=%MAX_FILE_SIZE%
echo MAX_REQUEST_SIZE=%MAX_REQUEST_SIZE%
echo ========================================
echo.

echo Variáveis carregadas com sucesso!
echo Para iniciar a aplicação, execute: mvn spring-boot:run

REM Manter console aberto
if "%1"=="--keep-open" pause 