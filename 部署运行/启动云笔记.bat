@echo off
rem ============================================================
rem  CloudNote one-click launcher  (double-click this file)
rem
rem  NOTE: this .bat is intentionally ASCII-only.
rem  Chinese text inside a .bat is mangled by the cmd code page,
rem  so every Chinese message is printed by the PowerShell script
rem  that sits next to this file.
rem ============================================================
setlocal
cd /d "%~dp0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" set "PS=powershell.exe"

rem Find the launcher script by extension instead of hard-coding its
rem (Chinese) file name - that avoids encoding problems completely.
set "SCRIPT="
for %%F in ("%~dp0*.ps1") do if not defined SCRIPT set "SCRIPT=%%~fF"

if not defined SCRIPT (
    echo.
    echo [ERROR] No .ps1 launcher found in this folder.
    echo         Keep this .bat together with the launcher .ps1
    echo.
    pause
    exit /b 1
)

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
