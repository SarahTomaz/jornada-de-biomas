@echo off
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 goto sem_java
where javac >nul 2>&1
if errorlevel 1 goto sem_java

powershell -NoProfile -Command "$fontes = @(Get-ChildItem -LiteralPath src -Recurse -Filter *.java | ForEach-Object { $_.FullName }); & javac --release 17 -Xlint:all -encoding UTF-8 -d out $fontes; exit $LASTEXITCODE"
if errorlevel 1 goto erro_compilacao

java "-Dfile.encoding=UTF-8" -cp out jogo.Main
if errorlevel 1 goto erro_execucao
exit /b 0

:sem_java
echo O Java e o JDK 17 ou superior precisam estar instalados.
pause
exit /b 1

:erro_compilacao
echo Nao foi possivel compilar o jogo. Confira o JDK instalado.
pause
exit /b 1

:erro_execucao
echo Nao foi possivel abrir o jogo.
pause
exit /b 1
