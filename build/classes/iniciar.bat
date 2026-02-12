@echo off
title Sistema Cadastro de Cliente

cd /d %~dp0

java -jar Sistema.jar

if errorlevel 1 (
    echo.
    echo Ocorreu um erro ao iniciar o sistema.
    pause
)