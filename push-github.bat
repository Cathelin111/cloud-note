@echo off
rem ============================================
rem  One-click upload to GitHub (double-click me)
rem ============================================
rem Use full paths so it works even when System32 is missing from PATH
"%SystemRoot%\System32\chcp.com" 65001 >nul
cd /d "%~dp0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" set "PS=powershell.exe"

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0push-github.ps1" %*
echo.
pause
