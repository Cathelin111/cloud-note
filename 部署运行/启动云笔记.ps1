# ============================================================
#  云笔记 CloudNote —— 一键启动（免构建，直接运行）
#
#  用法：双击同目录的 启动云笔记.bat
#       或 powershell -ExecutionPolicy Bypass -File .\启动云笔记.ps1
#
#  说明：
#    * cloudnote-backend-0.1.0-SNAPSHOT.jar 是"胖包"，
#      里面已经包含全部第三方 jar（Spring Boot / MyBatis / MySQL 驱动 / JWT / Jackson…）
#      以及前端页面，所以只需要一个 JDK 17+ 和一个 MySQL 就能跑
#    * 首次运行前请先导入 cloud_note.sql 建库
# ============================================================
param(
    [int]$Port = 8081,
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Jar  = Join-Path $Root 'cloudnote-backend-0.1.0-SNAPSHOT.jar'
$Sql  = Join-Path $Root 'cloud_note.sql'

function Say([string]$m, [string]$c = 'Gray') { Write-Host $m -ForegroundColor $c }

function Test-Port([int]$p) {
    $c = New-Object System.Net.Sockets.TcpClient
    try { $c.Connect('127.0.0.1', $p); return $true }
    catch { return $false }
    finally { $c.Close() }
}

Say "============================================================" 'Cyan'
Say "  云笔记 CloudNote 启动器" 'Cyan'
Say "============================================================" 'Cyan'
Say ""

if (-not (Test-Path $Jar)) {
    Say "找不到程序包: $Jar" 'Red'
    Say "请确认本脚本与 cloudnote-backend-0.1.0-SNAPSHOT.jar 在同一个目录。" 'Yellow'
    Read-Host "按回车退出"
    return
}

# 1) 找 JDK 17+（本机可能同时装了 JDK 8，必须挑 17 以上的）
$java = $null
$cands = @()
if ($env:JAVA_HOME) { $cands += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
foreach ($base in @('C:\Program Files\Java', 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Microsoft', 'D:\Java')) {
    if (Test-Path $base) {
        $cands += (Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
                   Sort-Object Name -Descending | ForEach-Object { Join-Path $_.FullName 'bin\java.exe' })
    }
}
$cmdJava = (Get-Command java.exe -ErrorAction SilentlyContinue).Source
if ($cmdJava) { $cands += $cmdJava }

foreach ($c in $cands) {
    if ($c -and (Test-Path $c)) {
        $ver = ''
        try { $ver = (cmd /c "`"$c`" -version 2>&1" | Select-Object -First 1) } catch { $ver = '' }
        if ("$ver" -match 'version "(\d+)' -and [int]$Matches[1] -ge 17) { $java = $c; break }
    }
}
if (-not $java) {
    Say "没有找到 JDK 17 或更高版本（Spring Boot 3 不支持 JDK 8）。" 'Red'
    Say "请安装 JDK 17/21/25（推荐 https://adoptium.net 或 Oracle JDK）后重新运行。" 'Yellow'
    Say "如果已经装了但没识别到，可以手动指定，例如：" 'Yellow'
    Say '  $env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\启动云笔记.ps1' 'DarkGray'
    Read-Host "按回车退出"
    return
}
Say "使用 JDK: $java" 'Green'

# 2) 检查 MySQL
if (-not (Test-Port 3306)) {
    Say "[警告] 本机 3306 端口没有 MySQL 在监听。" 'Yellow'
    Say "        请先启动 MySQL 服务（Windows 服务名可能是 mysql80 / mysql96 / MySQL）。" 'Yellow'
    Say "        第一次运行还需要建库：" 'Yellow'
    Say "          mysql -uroot -p -e `"source $Sql`"" 'DarkGray'
    Say ""
}

# 3) 启动
if (Test-Port $Port) {
    Say "端口 $Port 已有服务在监听，直接打开浏览器即可。" 'Yellow'
} else {
    Say "正在启动后端（端口 $Port）…… 首次启动约 10-20 秒" 'Cyan'
    $out = Join-Path $Root 'run.log'
    $err = Join-Path $Root 'run.err.log'
    Start-Process -FilePath $java -ArgumentList @('-jar', $Jar, "--server.port=$Port") `
        -WorkingDirectory $Root -WindowStyle Hidden `
        -RedirectStandardOutput $out -RedirectStandardError $err | Out-Null

    $deadline = (Get-Date).AddSeconds(120)
    while ((Get-Date) -lt $deadline -and -not (Test-Port $Port)) { Start-Sleep -Milliseconds 800 }
    if (-not (Test-Port $Port)) {
        Say "启动失败或超时，请看日志：$err" 'Red'
        if (Test-Path $err) { Get-Content $err -Tail 20 }
        Read-Host "按回车退出"
        return
    }
}

$url = "http://localhost:$Port/"
Say ""
Say "============================================================" 'Green'
Say "  启动成功，访问地址： $url" 'Green'
Say "  测试账号： admin/admin123（管理员）、demo/123456（普通用户）" 'Green'
Say "  游客不需要登录，直接看【分享广场】和【社区活动】" 'Green'
Say "============================================================" 'Green'
Say ""
Say "关闭服务：在任务管理器结束 java.exe，或执行  taskkill /IM java.exe /F" 'DarkGray'
Say ""

if (-not $NoBrowser) { Start-Process $url }
Read-Host "按回车退出本窗口（服务会继续在后台运行）"
