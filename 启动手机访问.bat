@echo off
rem ============================================================
rem  CloudNote public-access tunnel launcher (double-click me)
rem
rem  This .bat is ASCII-only on purpose: Chinese text inside a .bat
rem  is mangled by the cmd code page and then executed as commands.
rem  All Chinese messages are printed by the PowerShell script.
rem ============================================================
setlocal
cd /d "%~dp0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" set "PS=powershell.exe"

rem Pick the tunnel script by extension (the only .ps1 here that is not
rem the GitHub helper), so no Chinese file name is hard-coded - that
rem avoids encoding problems completely.
set "SCRIPT="
for %%F in (*.ps1) do (
    echo %%F | findstr /i /c:"push-github" >nul
    if errorlevel 1 if not defined SCRIPT set "SCRIPT=%%~fF"
)

if not defined SCRIPT (
    echo.
    echo [ERROR] launcher .ps1 not found in this folder.
    echo.
    pause
    exit /b 1
)

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
echo.
pause
