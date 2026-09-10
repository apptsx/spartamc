@echo off
setlocal enabledelayedexpansion

title Sparta Compiler

echo ========================================
echo   Compilando Sparta Server
echo ========================================
echo.

cd /d "%~dp0"

:: ============================================
:: CONFIGURAÇÕES DO SERVIDOR
:: ============================================
set "SSH_HOST=SEU_SERVIDOR.COM"
set "SSH_USER=root"
set "SSH_PASS=SUA_SENHA_SSH"
set "SSH_PORT=22"

:: ============================================
:: 1. COMPILAÇÃO
:: ============================================
echo.
echo [1/4] Compilando modulos...
echo.

call gradlew.bat :core-bungee:build :core:build :core-bukkit:build :lobby:build :pvp:build :bedwars:build :duels:build :hungergames:build :auth:build

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo [OK] Compilacao concluida!

:: ============================================
:: 2. COPIAR JARs LOCALMENTE
:: ============================================
echo.
echo [2/4] Copiando JARs localmente...
echo.

set "BUILD_DIR=%CD%\build_temp"
set "LOCAL_PLUGINS=%CD%\plugins"

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%LOCAL_PLUGINS%" rmdir /s /q "%LOCAL_PLUGINS%"

mkdir "%BUILD_DIR%"
mkdir "%LOCAL_PLUGINS%"

call :copy_jar "core-bungee" "core-bungee"
call :copy_jar "lobby" "lobby"
call :copy_jar "bedwars" "bedwars"
call :copy_jar "pvp" "pvp"
call :copy_jar "duels" "duels"
call :copy_jar "hungergames" "hungergames"
call :copy_jar "auth" "auth"

:: ============================================
:: 3. GERAR SCRIPT PS1 PARA ENVIO
:: ============================================
echo.
echo [3/4] Enviando para o servidor remoto...
echo.

:: Criar script PowerShell temporário
set "PS_SCRIPT=%TEMP%\deploy_%RANDOM%.ps1"

(
echo $password = ConvertTo-SecureString "%SSH_PASS%" -AsPlainText -Force
echo $credential = New-Object System.Management.Automation.PSCredential("%SSH_USER%", $password)
echo.
echo $sessions = @()
echo.
echo # Criar sessões PSSession
echo $BungeeSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $AuthSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $BedwarsSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $DuelsSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $HungergamesSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $PvpSession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo $LobbySession = New-PSSession -HostName "%SSH_HOST%" -UserName "%SSH_USER%" -KeyFilePath $null -Port %SSH_PORT% -Credential $credential -ErrorAction SilentlyContinue
echo.
echo # Função para remover arquivos antigos
echo function Remove-OldJar {
echo     param($Session, $RemotePath, $JarName)
echo     Invoke-Command -Session $Session -ScriptBlock {
echo         param($path, $name)
echo         if (Test-Path $path) {
echo             Get-ChildItem $path -Filter "$name*.jar" ^| ForEach-Object {
echo                 Remove-Item $_.FullName -Force
echo                 Write-Host "        Removido: $($_.Name)"
echo             }
echo         }
echo     } -ArgumentList $RemotePath, $JarName
echo }
echo.
echo # Função para enviar arquivo
echo function Send-Jar {
echo     param($Session, $LocalFile, $RemotePath, $JarName)
echo     if (Test-Path $LocalFile) {
echo         $RemoteFile = "$RemotePath\$JarName.jar"
echo         Invoke-Command -Session $Session -ScriptBlock {
echo             param($path, $file)
echo             if (!(Test-Path $path)) {
echo                 New-Item -ItemType Directory -Path $path -Force ^| Out-Null
echo             }
echo         } -ArgumentList $RemotePath, $RemoteFile
echo         Copy-Item -ToSession $Session -Path $LocalFile -Destination $RemoteFile -Force
echo         Write-Host "        Enviado: $JarName.jar"
echo     } else {
echo         Write-Host "        [ERRO] $JarName.jar nao encontrado" -ForegroundColor Red
echo     }
echo }
echo.
echo Write-Host "    Enviando core-bungee..." -ForegroundColor Cyan
echo Send-Jar $BungeeSession "%BUILD_DIR%\core-bungee.jar" "/home/Bungee/plugins" "core-bungee"
echo.
echo Write-Host "    Enviando auth..." -ForegroundColor Cyan
echo Send-Jar $AuthSession "%BUILD_DIR%\auth.jar" "/home/auth/plugins" "auth"
echo.
echo Write-Host "    Enviando mini games..." -ForegroundColor Cyan
echo Send-Jar $BedwarsSession "%BUILD_DIR%\bedwars.jar" "/home/bedwars/plugins" "bedwars"
echo Send-Jar $DuelsSession "%BUILD_DIR%\duels.jar" "/home/duels/plugins" "duels"
echo Send-Jar $HungergamesSession "%BUILD_DIR%\hungergames.jar" "/home/hungergames/plugins" "hungergames"
echo Send-Jar $PvpSession "%BUILD_DIR%\pvp.jar" "/home/pvp/plugins" "pvp"
echo.
echo Write-Host "    Enviando lobbies..." -ForegroundColor Cyan
echo Send-Jar $LobbySession "%BUILD_DIR%\lobby.jar" "/home/lobby/plugins" "lobby"
echo Send-Jar $LobbySession "%BUILD_DIR%\lobby.jar" "/home/lobbyduels/plugins" "lobby"
echo Send-Jar $LobbySession "%BUILD_DIR%\lobby.jar" "/home/lobbybw/plugins" "lobby"
echo Send-Jar $LobbySession "%BUILD_DIR%\lobby.jar" "/home/lobbyhg/plugins" "lobby"
echo Send-Jar $LobbySession "%BUILD_DIR%\lobby.jar" "/home/lobbypvp/plugins" "lobby"
echo.
echo # Fechar sessões
echo Get-PSSession ^| Remove-PSSession
echo Write-Host "    ^✓ Todos os arquivos enviados!" -ForegroundColor Green
) > "%PS_SCRIPT%"

:: Executar script PowerShell
powershell -ExecutionPolicy Bypass -File "%PS_SCRIPT%"

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha no envio!
    echo.
    echo [ALTERNATIVA] Envie manualmente usando WinSCP ou SFTP
    echo Arquivos estao em: %BUILD_DIR%
    pause
    exit /b 1
)

:: ============================================
:: 4. LIMPEZA
:: ============================================
echo.
echo [4/4] Limpando arquivos temporarios...
del "%PS_SCRIPT%" 2>nul
rmdir /s /q "%BUILD_DIR%" 2>nul

:: ============================================
:: FINALIZAR
:: ============================================
echo.
echo ========================================
echo   Build completo!
echo ========================================
echo.
echo Todos os plugins foram enviados para o servidor!
echo.
pause
exit /b 0

:: ============================================
:: FUNÇÕES AUXILIARES
:: ============================================
:copy_jar
set "module=%1"
set "name=%2"
if exist "%module%\build\libs\%name%.jar" (
    copy /y "%module%\build\libs\%name%.jar" "%BUILD_DIR%\" >nul
    copy /y "%module%\build\libs\%name%.jar" "%LOCAL_PLUGINS%\" >nul
    echo     ✓ %name%.jar
) else (
    echo     ✗ %name%.jar [NAO ENCONTRADO]
)
exit /b