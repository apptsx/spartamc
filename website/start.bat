@echo off
echo Iniciando SpartaMC...

:: Mata qualquer processo usando a porta 3000
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000 ^| findstr LISTENING') do (
    echo Matando processo na porta 3000 (PID %%a)...
    taskkill /PID %%a /F >nul 2>&1
)

:: Inicia o servidor em background
start "SpartaMC Server" cmd /k "cd server && node server.js"

:: Aguarda 2 segundos para o servidor subir
timeout /t 2 /nobreak >nul

:: Abre no navegador
start "" "http://localhost:3000"

echo Servidor rodando em http://localhost:3000
