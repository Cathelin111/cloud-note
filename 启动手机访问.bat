@echo off
rem ============================================
rem  云笔记(新版) 手机公网访问 (双击我)
rem  双击即可: 自动启动前后端 + 开公网隧道, 并打印手机访问网址
rem ============================================
"%SystemRoot%\System32\chcp.com" 65001 >nul
cd /d "%~dp0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" set "PS=powershell.exe"

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0启动手机公网访问.ps1" %*
echo.
pause
