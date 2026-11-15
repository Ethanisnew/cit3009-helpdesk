@echo off
title Helpdesk Launcher
echo =========================================
echo   Starting University Help Desk System
echo =========================================

:: 1. Start the Backend
echo Starting Backend...
start "Helpdesk Backend" cmd /k "cd /d "%~dp0helpdesk-backend" && .\mvnw.cmd spring-boot:run -DskipTests"

:: 2. Wait for the backend to boot up and connect to MySQL
echo Waiting 8 seconds for Spring Boot to initialize...
timeout /t 8 /nobreak >nul

:: 3. Start the Frontend
echo Starting Frontend...
start "Helpdesk Frontend" cmd /k "cd /d "%~dp0helpdesk-client" && ..\helpdesk-backend\mvnw.cmd -f pom.xml javafx:run"

echo =========================================
echo   System Started! Check the two windows.
echo =========================================
timeout /t 3 >nul
exit