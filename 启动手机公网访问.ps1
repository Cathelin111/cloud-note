# ============================================================
#  云笔记(新版) —— 一键让手机在“任意网络”下访问
#
#  作用:
#    1) 检查/启动 新版后端(Spring Boot 8081) 与 前端(Vite 5173)
#    2) 建一条公网隧道, 把 5173 映射成一个公网 https 网址
#    3) 打印手机可以直接打开的网址, 并写入 手机访问地址.txt
#    4) 守护进程: 每 45 秒自检, 掉线自动重建(重建后网址可能变化, 会重新打印)
#
#  用法:
#    双击 启动手机访问.bat
#    或: powershell -ExecutionPolicy Bypass -File 启动手机公网访问.ps1
#    常用参数:
#      -Tunnel cf      默认: Cloudflare 快速隧道(cloudflared), 无广告/提示页
#      -Tunnel serveo  备选: 不需要额外程序, 但手机首次打开有一次 “Continue to Site” 提示页
#      -Tunnel pinggy  备选: 免费版约 60 分钟一次, 同样有提示页
#      -Tunnel lhr     备选: localhost.run, 实测不稳(容易 no tunnel here)
#      -NoStart        不自动启动前后端, 只开隧道(适合已经手动起好了)
#      -NoWatch        不做掉线守护
#      -Stop           关闭已开的隧道
#
#  注意:
#    * 只暴露前端 5173; 后端 8081 与 MySQL 3306 都留在本机, 由 Vite 代理转发, 不暴露公网
#    * 电脑要保持开机, 本窗口不要关
#    * 免费隧道每次重建都会换一个网址(重跑脚本请以 手机访问地址.txt 为准)
# ============================================================
param(
    [ValidateSet('cf', 'serveo', 'pinggy', 'lhr')]
    [string]$Tunnel = 'cf',
    [int]$FrontPort = 5173,
    [int]$BackPort = 8081,
    [switch]$Stop,
    [switch]$NoStart,
    [switch]$NoWatch
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

# 找 cloudflared.exe(Cloudflare 隧道客户端, 约 55MB, 不属于仓库)
function Find-Cloudflared {
    $cands = @(
        (Join-Path $Root '.tools\cloudflared.exe'),
        'E:\cloudnote\_tools\cloudflared.exe',
        (Join-Path $env:LOCALAPPDATA 'cloudflared\cloudflared.exe'),
        (Join-Path $env:ProgramFiles 'cloudflared\cloudflared.exe'),
        (Join-Path $env:USERPROFILE 'cloudflared.exe')
    )
    foreach ($c in $cands) {
        if ($c -and (Test-Path $c)) { return $c }
    }
    $cmd = Get-Command cloudflared.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

function Get-TunnelPid {
    if (-not (Test-Path $PidFile)) { return $null }
    $raw = (Get-Content $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
    $procId = 0
    if ([int]::TryParse($raw, [ref]$procId)) { return $procId }
    return $null
}

function Stop-Tunnel {
    $procId = Get-TunnelPid
    if ($procId -and (Get-Process -Id $procId -ErrorAction SilentlyContinue)) {
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        Say "已关闭隧道进程 (PID $procId)" 'Green'
    } else {
        Say "没有发现正在运行的隧道。" 'Yellow'
    }
    if (Test-Path $PidFile) { Remove-Item $PidFile -Force -ErrorAction SilentlyContinue }
}

# 从隧道输出里提取公网网址
function Get-PublicUrl {
    if (-not (Test-Path $OutLog)) { return $null }
    $txt = Get-Content $OutLog -Raw -ErrorAction SilentlyContinue
    $txt2 = Get-Content $ErrLog -Raw -ErrorAction SilentlyContinue
    $all = "$txt$txt2"
    if (-not $all) { return $null }
    $urls = [regex]::Matches($all, 'https://[A-Za-z0-9\.\-]+') | ForEach-Object { $_.Value } |
            Where-Object { $_ -notmatch '://(api|www|developers|admin)\.' }
    foreach ($u in $urls) {
        if ($u -match '^https://[A-Za-z0-9\-]+\.trycloudflare\.com$') { return $u }
    }
    foreach ($u in $urls) {
        if ($u -match '^https://[A-Za-z0-9\-]+\.serveousercontent\.com$') { return $u }
    }
    foreach ($u in $urls) {
        if ($u -match '^https://[A-Za-z0-9\-]+\.lhr\.life$') { return $u }
    }
    foreach ($u in $urls) {
        if ($u -match '^https://[A-Za-z0-9\-]+\.(free\.pinggy\.net|run\.pinggy-free\.link)$') { return $u }
    }
    return $null
}

# 从本机访问公网网址, 判断隧道是否真的通(优先用 node, 避免部分环境下 TLS 受限)
function Test-PublicUrl([string]$baseUrl) {
    $node = (Get-Command node -ErrorAction SilentlyContinue)
    if ($node) {
        $js = "fetch(process.argv[1],{signal:AbortSignal.timeout(15000)}).then(r=>process.exit(r.ok?0:2)).catch(()=>process.exit(3))"
        $code = 3
        try {
            & $node.Source -e $js "$baseUrl/api/ping" 2>$null | Out-Null
            $code = $LASTEXITCODE
        } catch {
            $code = 3
        }
        return ($code -eq 0)
    }
    try {
        $r = Invoke-WebRequest -Uri "$baseUrl/api/ping" -TimeoutSec 15 -UseBasicParsing -ErrorAction Stop
        return ($r.StatusCode -eq 200)
    } catch {
        return $false
    }
}

function Start-TunnelProcess([string]$kind, [int]$port) {
    if (Test-Path $OutLog) { Remove-Item $OutLog -Force -ErrorAction SilentlyContinue }
    if (Test-Path $ErrLog) { Remove-Item $ErrLog -Force -ErrorAction SilentlyContinue }

    if ($kind -eq 'cf') {
        $cf = Find-Cloudflared
        # cloudflared 默认走 QUIC(UDP), 部分网络会被挡; 指定 http2(TCP) 更稳
        $cfArgs = @('tunnel', '--url', "http://localhost:$port", '--no-autoupdate', '--protocol', 'http2')
        $p = Start-Process -FilePath $cf -ArgumentList $cfArgs -WindowStyle Hidden `
            -RedirectStandardOutput $OutLog -RedirectStandardError $ErrLog -PassThru
        Set-Content -Path $PidFile -Value $p.Id -Encoding ASCII
        return $p
    }

    $common = @('-o', 'StrictHostKeyChecking=accept-new',
                '-o', 'ServerAliveInterval=20',
                '-o', 'ServerAliveCountMax=3',
                '-o', 'ExitOnForwardFailure=yes')

    if ($kind -eq 'pinggy') {
        $sshArgs = @('-p', '443') + $common + @("-R0:localhost:$port", 'a.pinggy.io')
    } elseif ($kind -eq 'lhr') {
        $sshArgs = $common + @('-R', "80:localhost:$port", 'nokey@localhost.run')
    } else {
        $sshArgs = $common + @('-R', "80:localhost:$port", 'serveo.net')
    }

    $p = Start-Process -FilePath $SshExe -ArgumentList $sshArgs -WindowStyle Hidden `
        -RedirectStandardOutput $OutLog -RedirectStandardError $ErrLog -PassThru
    Set-Content -Path $PidFile -Value $p.Id -Encoding ASCII
    return $p
}

function Write-UrlFile([string]$u, [string]$kind) {
    $stamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $tunnelName = switch ($kind) {
        'cf'     { 'Cloudflare 快速隧道 (cloudflared)' }
        'pinggy' { 'Pinggy' }
        'lhr'    { 'localhost.run' }
        default  { 'serveo' }
    }
    $tip = '手机直接打开即可'
    if ($kind -eq 'serveo' -or $kind -eq 'pinggy') {
        $tip = '手机首次打开会先出现提示页, 点一下 "Continue to Site" 即可进站'
    }
    @"
云笔记(新版) 手机公网访问地址
生成时间: $stamp
隧道方式: $tunnelName
访问网址: $u

说明:
  1. 手机连 4G/5G 或任意 Wi-Fi 都能打开(不需要和电脑同一个网络)
  2. 电脑必须保持开机, 且本脚本窗口不要关闭
  3. $tip
  4. 隧道重建后网址会变, 以本文件为准(脚本会重新打印)
  5. 只暴露前端 5173, 后端 8081 与 MySQL 不对外网开放
"@ | Set-Content -Path $UrlFile -Encoding UTF8
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
if ($Tunnel -eq 'cf') {
    if (-not (Find-Cloudflared)) {
        Say "没找到 cloudflared.exe(Cloudflare 隧道客户端)。" 'Red'
        Say "任选一种方式准备好它, 然后重跑本脚本:" 'Yellow'
        Say "  A) 下载到 E:\cloudnote\_tools\ (约 55MB):" 'Yellow'
        Say "     Invoke-WebRequest 'https://gh.llkk.cc/https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'E:\cloudnote\_tools\cloudflared.exe'" 'Yellow'
        Say "  B) 或改用不需要额外程序的隧道: -Tunnel serveo" 'Yellow'
        return
    }
} elseif (-not (Test-Path $SshExe)) {
    Say "找不到 Windows 自带的 ssh.exe: $SshExe" 'Red'
    Say "请先用管理员 PowerShell 执行(或改用 -Tunnel cf):" 'Yellow'
    Say "  Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0" 'Yellow'
    return
}

if (-not (Test-Port 3306)) {
    Say "[警告] 本机 3306 (MySQL) 没在监听, 后端可能连不上数据库。" 'Yellow'
    Say "       启动数据库: net start mysql96" 'Yellow'
}

if (-not $NoStart) {
    # 2) 后端 8081
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
                # java -version 输出到 stderr, 直接 & 会被 ErrorActionPreference=Stop 当错误中断,
                # 所以经 cmd 取输出并吃掉退出码
                $ver = ''
                try { $ver = (cmd /c "`"$c`" -version 2>&1" | Select-Object -First 1) } catch { $ver = '' }
                if ("$ver" -match 'version "(\d+)' -and [int]$Matches[1] -ge 17) { $java = $c; break }
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

# 4) 建立隧道 + 掉线守护
#    按顺序尝试: 首选隧道 -> 其余隧道; 只有"真的能访问"的网址才会被采用
$order = @($Tunnel) + (@('cf', 'serveo', 'pinggy', 'lhr') | Where-Object { $_ -ne $Tunnel })
$idx = 0
$tries = 0            # 当前隧道已尝试次数
$attempt = 0          # 总尝试次数
$lastUrl = $null

while ($true) {
    $kind = $order[$idx]
    $attempt++
    $tries++
    Say "[3/3] 建立公网隧道 ($kind, 第 $attempt 次) ..." 'Cyan'

    $proc = Start-TunnelProcess -kind $kind -port $FrontPort

    # 拿网址 + 实测能不能通(网址出现不等于隧道可用)
    $url = $null
    $deadline = (Get-Date).AddSeconds(90)
    while ((Get-Date) -lt $deadline) {
        $cand = Get-PublicUrl
        if ($cand) {
            if (Test-PublicUrl $cand) { $url = $cand; break }
        }
        if ($proc.HasExited) { break }
        Start-Sleep -Milliseconds 1000
    }

    if (-not $url) {
        Say "这个隧道没能拿到可用网址, 输出如下:" 'Red'
        if (Test-Path $ErrLog) { Get-Content $ErrLog -Tail 8 }
        if (Test-Path $OutLog) { Get-Content $OutLog -Tail 8 }
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        if ($tries -ge 2) {
            if ($idx -lt $order.Count - 1) {
                $idx++
                $tries = 0
                Say "改用 $($order[$idx]) 隧道重试 ..." 'Yellow'
            } else {
                Say "所有隧道都试过了, 都没成功。请检查网络后重跑脚本。" 'Red'
                return
            }
        }
        Start-Sleep -Seconds 5
        continue
    }
    $tries = 0

    Write-UrlFile -u $url -kind $kind

    if ($url -ne $lastUrl) {
        Say ""
        Say "============================================================" 'Green'
        Say "  手机访问地址(任意网络可用):" 'Green'
        Say "  $url" 'White'
        Say "============================================================" 'Green'
        Say ""
        Say "  已写入文件: 手机访问地址.txt" 'DarkGray'
        if ($kind -eq 'serveo' -or $kind -eq 'pinggy') {
            Say "  手机第一次打开会先看到一个提示页, 点 “Continue to Site” 即可进站。" 'DarkGray'
        }
        Say "  演示账号: admin/admin123 (管理员)、demo/123456 (普通用户)" 'DarkGray'
        Say "  关闭公网访问: 执行本脚本并加 -Stop" 'DarkGray'
        Say "  本窗口请保持打开(关掉窗口公网访问会中断)" 'Yellow'
        Say ""
    } else {
        Say "隧道已恢复, 网址不变: $url" 'Green'
    }
    $lastUrl = $url

    if ($NoWatch) {
        Say "已指定 -NoWatch: 不守护, 脚本退出后隧道仍在后台运行。" 'DarkGray'
        return
    }

    # 守护循环: 每 45 秒自检一次, 连续 3 次不通就重建
    $fail = 0
    while ($true) {
        Start-Sleep -Seconds 45
        $alive = [bool](Get-Process -Id $proc.Id -ErrorAction SilentlyContinue)
        if ($alive -and (Test-PublicUrl $url)) {
            if ($fail -gt 0) { Say "[$(Get-Date -Format 'HH:mm:ss')] 隧道已恢复正常。" 'Green' }
            $fail = 0
            continue
        }
        $fail++
        $failNote = ''
        if (-not $alive) { $failNote = ' [隧道进程已退出]' }
        Say "[$(Get-Date -Format 'HH:mm:ss')] 隧道自检未通过 ($fail/3)$failNote" 'Yellow'
        if ($fail -ge 3) {
            Say "重建隧道中 ..." 'Yellow'
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            break
        }
    }
}
