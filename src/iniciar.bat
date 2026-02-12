@echo off
title Sistema Cadastro de Cliente

cd /d %~dp0

java -jar .\SisGerencialMaster.jar

if errorlevel 1 (
    echo.
    echo Ocorreu um erro ao iniciar o sistema.
    pause
)
