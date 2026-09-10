# ============================================================
#  云笔记(新版) —— 一键让手机在“任意网络”下访问
#
#  作用:
#    1) 检查/启动 新版后端(Spring Boot 8081) 与 前端(Vite 5173)
#    2) 用 SSH 反向隧道把 5173 映射到一个公网 https 地址
#    3) 打印手机可以直接打开的网址, 并写入 手机访问地址.txt
#
#  用法:
#    双击 启动手机访问.bat
#    或: powershell -ExecutionPolicy Bypass -File 启动手机公网访问.ps1
#    常用参数:
#      -Tunnel pinggy   换用 Pinggy 隧道(默认用 localhost.run)
#      -NoStart         不自动启动前后端, 只开隧道(适合已经手动起好了)
#      -Stop            关闭已开的隧道
#
#  注意:
#    * 只暴露前端 5173; 后端 8081 与 MySQL 3306 都留在本机, 由 Vite 代理转发, 不暴露公网
#    * 电脑要保持开机, 本窗口不要关; 免费隧道偶尔掉线, 重跑一次即可(网址会变)
# ============================================================
param(
    [ValidateSet('lhr', 'pinggy')]
    [string]$Tunnel = 'lhr',
    [int]$FrontPort = 5173,
    [int]$BackPort = 8081,
    [switch]$Stop,
    [switch]$NoStart
)

$ErrorActionPreference = 'Stop'

$Root      = Split-Path -Parent $MyInvocation.MyCommand.Path
$RunDir    = Join-Path $Root '.tunnel'
$PidFile   = Join-Path $RunDir 'tunnel.pid'
$OutLog    = Join-Path $RunDir 'tunnel.out.log'
$ErrLog    = Join-Path $RunDir 'tunnel.err.log'
$UrlFile   = Join-Path $Root '手机访问地址.txt'
$SshExe    = Join-Path $env:WINDIR 'System32\OpenSSH\ssh.exe'

if (-not (Test-Path $RunDir)) { New-Item -ItemType Directory -Path $RunDir -Force | Out-Null }

function Say([string]$msg, [string]$color = 'Gray') { Write-Host $msg -ForegroundColor $color }

function Test-Port([int]$port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $client.Connect('127.0.0.1', $port)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Wait-Port([int]$port, [int]$timeoutSec) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-Port $port) { return $true }
        Start-Sleep -Milliseconds 800
    }
    return (Test-Port $port)
}

function Get-TunnelPid {
    if (-not (Test-Path $PidFile)) { return $null }
    $raw = (Get-Content $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
    $id = 0
    if ([int]::TryParse($raw, [ref]$id)) { return $id }
    return $null
}

function Stop-Tunnel {
    $id = Get-TunnelPid
    if ($id -and (Get-Process -Id $id -ErrorAction SilentlyContinue)) {
        Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
        Say "已关闭隧道进程 (PID $id)" 'Green'
    } else {
        Say "没有发现正在运行的隧道。" 'Yellow'
    }
    if (Test-Path $PidFile) { Remove-Item $PidFile -Force -ErrorAction SilentlyContinue }
}

function Get-PublicUrl {
    if (-not (Test-Path $OutLog)) { return $null }
    $txt = Get-Content $OutLog -Raw -ErrorAction SilentlyContinue
    if (-not $txt) { return $null }
    $urls = [regex]::Matches($txt, 'https://[A-Za-z0-9\.\-]+') | ForEach-Object { $_.Value }
    foreach ($u in $urls) {
        if ($u -match '\.lhr\.life$') { return $u }
    }
    foreach ($u in $urls) {
        if ($u -match 'pinggy') { return $u }
    }
    return $null
}

# ------------------------------------------------------------
if ($Stop) {
    Stop-Tunnel
    return
}

Say "============================================================" 'Cyan'
Say "  云笔记(新版) 手机公网访问 —— 一键启动" 'Cyan'
Say "============================================================" 'Cyan'
Say ""

# 0) 已有隧道在跑就不再开第二个
$oldId = Get-TunnelPid
if ($oldId -and (Get-Process -Id $oldId -ErrorAction SilentlyContinue)) {
    $oldUrl = Get-PublicUrl
    Say "已经有一个隧道在运行 (PID $oldId)。" 'Yellow'
    if ($oldUrl) { Say "当前手机访问地址: $oldUrl" 'Green' }
    Say "如需重开, 请先执行: powershell -ExecutionPolicy Bypass -File `"$($MyInvocation.MyCommand.Name)`" -Stop" 'Yellow'
    return
}

# 1) 依赖检查
if (-not (Test-Path $SshExe)) {
    Say "找不到 Windows 自带的 ssh.exe: $SshExe" 'Red'
    Say "请先用管理员 PowerShell 执行(或改用 Git Bash):" 'Yellow'
    Say "  Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0" 'Yellow'
    return
}

if (-not (Test-Port 3306)) {
    Say "[警告] 本机 3306 (MySQL) 没在监听, 后端可能连不上数据库。" 'Yellow'
    Say "       启动数据库: net start mysql96" 'Yellow'
}

# 2) 后端 8081
if (-not $NoStart) {
    if (Test-Port $BackPort) {
        Say "[1/3] 后端已在运行 (端口 $BackPort)" 'Green'
    } else {
        Say "[1/3] 启动后端 (端口 $BackPort) ..." 'Cyan'
        $jar = Join-Path $Root 'cloudnote-modern\backend\target\cloudnote-backend-0.1.0-SNAPSHOT.jar'
        if (-not (Test-Path $jar)) {
            Say "找不到后端程序包: $jar" 'Red'
            Say "请先构建: cd cloudnote-modern\backend; mvn clean package -DskipTests" 'Yellow'
            return
        }

        # 新版需要 JDK 17+, 而本机默认 JAVA_HOME 往往是 1.8(旧系统用的), 所以这里自己找
        $java = $null
        $cands = @()
        if ($env:JAVA_HOME) { $cands += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
        $cands += (Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
                   Sort-Object Name -Descending | ForEach-Object { Join-Path $_.FullName 'bin\java.exe' })
        foreach ($c in $cands) {
            if (Test-Path $c) {
                $ver = (& $c -version 2>&1 | Select-Object -First 1)
                if ($ver -match 'version "(\d+)' -and [int]$Matches[1] -ge 17) { $java = $c; break }
            }
        }
        if (-not $java) {
            Say "没找到 JDK 17 及以上版本, 无法启动新版后端。" 'Red'
            Say "请安装 JDK 17/21/25 后重试(旧系统的 1.8 不能跑 Spring Boot 3)。" 'Yellow'
            return
        }
        Say "      使用 JDK: $java"
        Start-Process -FilePath $java -ArgumentList @('-jar', $jar) `
            -WorkingDirectory (Split-Path -Parent (Split-Path -Parent $jar)) -WindowStyle Hidden `
            -RedirectStandardOutput (Join-Path $RunDir 'backend.out.log') `
            -RedirectStandardError  (Join-Path $RunDir 'backend.err.log') | Out-Null
        if (-not (Wait-Port $BackPort 90)) {
            Say "后端 90 秒内没起来, 请看日志: $(Join-Path $RunDir 'backend.err.log')" 'Red'
            return
        }
        Say "      后端已启动: http://localhost:$BackPort/api/ping" 'Green'
    }

    # 3) 前端 5173
    if (Test-Port $FrontPort) {
        Say "[2/3] 前端已在运行 (端口 $FrontPort)" 'Green'
    } else {
        Say "[2/3] 启动前端 (端口 $FrontPort) ..." 'Cyan'
        $feDir = Join-Path $Root 'cloudnote-modern\frontend-web'
        if (-not (Test-Path (Join-Path $feDir 'node_modules'))) {
            Say "前端依赖没装, 请先执行: cd cloudnote-modern\frontend-web; npm install" 'Red'
            return
        }
        $npm = (Get-Command npm.cmd -ErrorAction SilentlyContinue).Source
        if (-not $npm) { $npm = (Get-Command npm -ErrorAction SilentlyContinue).Source }
        if (-not $npm) { Say "找不到 npm, 请先安装 Node.js 18+" 'Red'; return }
        Start-Process -FilePath $npm -ArgumentList @('run', 'dev') -WorkingDirectory $feDir -WindowStyle Hidden `
            -RedirectStandardOutput (Join-Path $RunDir 'frontend.out.log') `
            -RedirectStandardError  (Join-Path $RunDir 'frontend.err.log') | Out-Null
        if (-not (Wait-Port $FrontPort 90)) {
            Say "前端 90 秒内没起来, 请看日志: $(Join-Path $RunDir 'frontend.err.log')" 'Red'
            return
        }
        Say "      前端已启动: http://localhost:$FrontPort" 'Green'
    }
} else {
    if (-not (Test-Port $FrontPort)) { Say "前端 $FrontPort 没在监听, 手机端会打不开。" 'Red'; return }
    Say "[1/3][2/3] 已跳过前后端启动 (-NoStart)" 'DarkGray'
}

# 4) 开隧道
Say "[3/3] 建立公网隧道 ($Tunnel) ..." 'Cyan'
if (Test-Path $OutLog) { Remove-Item $OutLog -Force -ErrorAction SilentlyContinue }
if (Test-Path $ErrLog) { Remove-Item $ErrLog -Force -ErrorAction SilentlyContinue }

if ($Tunnel -eq 'pinggy') {
    $remote = "0:localhost:$FrontPort"     # 让服务端随机分配端口
    $sshArgs = @('-p', '443',
                 '-o', 'StrictHostKeyChecking=accept-new',
                 '-o', 'ServerAliveInterval=30',
                 '-o', 'ExitOnForwardFailure=yes',
                 "-R$remote",
                 'a.pinggy.io')
} else {
    $remote = "80:localhost:$FrontPort"    # localhost.run 用 80 端口对外提供 https
    $sshArgs = @('-o', 'StrictHostKeyChecking=accept-new',
                 '-o', 'ServerAliveInterval=30',
                 '-o', 'ExitOnForwardFailure=yes',
                 '-R', $remote,
                 'nokey@localhost.run')
}

$proc = Start-Process -FilePath $SshExe -ArgumentList $sshArgs -WindowStyle Hidden `
    -RedirectStandardOutput $OutLog -RedirectStandardError $ErrLog -PassThru
Set-Content -Path $PidFile -Value $proc.Id -Encoding ASCII

$url = $null
$deadline = (Get-Date).AddSeconds(45)
while ((Get-Date) -lt $deadline) {
    $url = Get-PublicUrl
    if ($url) { break }
    if ($proc.HasExited) { break }
    Start-Sleep -Milliseconds 700
}

if (-not $url) {
    Say "没能拿到公网地址, 隧道输出如下:" 'Red'
    if (Test-Path $OutLog) { Get-Content $OutLog -Tail 20 }
    if (Test-Path $ErrLog) { Get-Content $ErrLog -Tail 20 }
    Say "可换一个隧道重试: -Tunnel pinggy" 'Yellow'
    return
}

$stamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
@"
云笔记(新版) 手机公网访问地址
生成时间: $stamp
访问网址: $url

说明:
  1. 手机连 4G/5G 或任意 Wi-Fi 都能打开(不需要和电脑同一个网络)
  2. 电脑必须保持开机, 且本脚本窗口不要关闭
  3. 免费隧道掉线后重跑一次脚本即可, 网址会变(以本文件为准)
  4. 只暴露前端 5173, 后端 8081 与 MySQL 不对外网开放
"@ | Set-Content -Path $UrlFile -Encoding UTF8

Say ""
Say "============================================================" 'Green'
Say "  手机访问地址(任意网络可用):" 'Green'
Say "  $url" 'White'
Say "============================================================" 'Green'
Say ""
Say "  已写入文件: 手机访问地址.txt" 'DarkGray'
Say "  演示账号: admin/admin123 (管理员)、demo/123456 (普通用户)" 'DarkGray'
Say "  关闭公网访问: 关掉本窗口, 或执行同目录脚本并加 -Stop" 'DarkGray'
Say ""
Say "  按 Ctrl+C 可退出本脚本(隧道仍在后台); 想彻底关闭请用 -Stop" 'Yellow'
Say ""

# 隧道保持运行期间, 脚本窗口保持打开(用轮询而非 Wait-Process, 兼容性更好)
while ($true) {
    if (-not (Get-Process -Id $proc.Id -ErrorAction SilentlyContinue)) { break }
    Start-Sleep -Seconds 2
}

Say ""
Say "隧道已结束(可能是掉线或被关闭)。重新运行本脚本即可再开一次。" 'Yellow'
