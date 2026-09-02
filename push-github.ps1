# ============================================================
#  一键上传到 GitHub
#  用法:
#    双击 push-github.bat           使用自动提交信息
#    或: powershell -File push-github.ps1 -Message "我的更新"
# ============================================================
param(
    [string]$Message = ""
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# ---------- 1. 定位 git ----------
$git = (Get-Command git.exe -ErrorAction SilentlyContinue).Source
if (-not $git) {
    $candidates = @(
        "$env:LOCALAPPDATA\Programs\Git\cmd\git.exe",
        "C:\Program Files\Git\cmd\git.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $git = $c; break }
    }
}
if (-not $git) {
    Write-Host "[错误] 未找到 git，请先安装 Git for Windows" -ForegroundColor Red
    exit 1
}

Push-Location $repoRoot
try {
    Write-Host ""
    Write-Host "========== 一键上传 GitHub ==========" -ForegroundColor Cyan
    Write-Host "仓库目录: $repoRoot"

    # ---------- 2. 确认是 git 仓库 ----------
    & $git rev-parse --is-inside-work-tree 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[错误] 当前目录不是 git 仓库" -ForegroundColor Red
        exit 1
    }

    # ---------- 3. 检查是否有改动 ----------
    $changed = (& $git status --porcelain) -ne $null
    $statusLines = @(& $git status --porcelain)
    if ($statusLines.Count -eq 0) {
        Write-Host ""
        Write-Host "[提示] 没有检测到任何改动，代码已是最新，无需上传。" -ForegroundColor Yellow
        Write-Host "       (若想强制重新提交请改代码后再运行)"
        exit 0
    }

    Write-Host ("检测到改动文件数: {0}" -f $statusLines.Count) -ForegroundColor Yellow

    # ---------- 4. 提交信息 ----------
    if ([string]::IsNullOrWhiteSpace($Message)) {
        $Message = "auto-commit $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    }

    # ---------- 5. 暂存 + 提交 ----------
    Write-Host ""
    Write-Host ">> 暂存所有改动 (git add -A) ..." -ForegroundColor Green
    & $git add -A
    if ($LASTEXITCODE -ne 0) { throw "git add 失败" }

    Write-Host ">> 提交: $Message" -ForegroundColor Green
    & $git commit -m $Message
    if ($LASTEXITCODE -ne 0) { throw "git commit 失败" }

    # ---------- 6. 推送（若远程有他人改动则自动合并后重试） ----------
    Write-Host ""
    Write-Host ">> 推送到 GitHub (git push) ..." -ForegroundColor Green
    & $git push
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   推送被拒绝，可能远程有新改动，尝试自动同步 (pull --rebase) ..." -ForegroundColor Yellow
        & $git pull --rebase origin main
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[错误] 自动合并失败！可能存在文件冲突，请手动处理：" -ForegroundColor Red
            Write-Host "       1. git status 查看冲突文件" -ForegroundColor Yellow
            Write-Host "       2. 解决冲突后: git add -A && git commit && git push" -ForegroundColor Yellow
            exit 1
        }
        Write-Host "   同步完成，重新推送 ..." -ForegroundColor Yellow
        & $git push
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[错误] 推送仍然失败！请检查网络或 SSH 密钥。" -ForegroundColor Red
            exit 1
        }
    }

    # ---------- 7. 完成 ----------
    Write-Host ""
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "  上传成功! " -ForegroundColor Green
    Write-Host "======================================" -ForegroundColor Cyan
    & $git log --oneline -3
    $remote = (& $git remote get-url origin).Trim()
    if ($remote -match 'github.com[:/](.+)\.git') {
        Write-Host ""
        Write-Host "GitHub 地址: https://github.com/$($matches[1])" -ForegroundColor Cyan
    }
}
catch {
    Write-Host ""
    Write-Host "[错误] $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
finally {
    Pop-Location
}
