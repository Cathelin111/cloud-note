@echo off
rem ============================================
rem  One-click upload to GitHub (double-click me)
rem ============================================
chcp 65001 >nul
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0push-github.ps1" %*
echo.
pause
