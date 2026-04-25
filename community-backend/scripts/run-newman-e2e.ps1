param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$CollectionPath = "..\\docs\\postman\\community-backend-e2e-regression.postman_collection.json",
    [string]$EnvironmentPath = "..\\docs\\postman\\community-backend.local.postman_environment.json"
)

$ErrorActionPreference = "Stop"

# 脚本目录作为基准，避免从不同目录执行时找不到文件
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path

Set-Location $projectRoot

# 使用更快的端口探测，避免 Test-NetConnection 在某些环境下等待过久
function Test-TcpPort {
    param(
        [string]$HostName = "127.0.0.1",
        [int]$Port = 8080,
        [int]$TimeoutMs = 1000
    )

    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($HostName, $Port, $null, $null)
        $ok = $iar.AsyncWaitHandle.WaitOne($TimeoutMs, $false)
        if (-not $ok) {
            $client.Close()
            return $false
        }
        $client.EndConnect($iar)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

# 从 BaseUrl 提取主机与端口，用于后端启动与就绪探测。
function Resolve-BaseEndpoint {
    param(
        [string]$Url
    )

    try {
        $uri = [System.Uri]$Url
    } catch {
        throw "BaseUrl 非法：$Url"
    }

    if (-not $uri.Host) {
        throw "BaseUrl 缺少主机：$Url"
    }
    if ($uri.Scheme -ne "http" -and $uri.Scheme -ne "https") {
        throw "BaseUrl 仅支持 http/https：$Url"
    }

    $resolvedHost = if ($uri.Host -eq "localhost") { "127.0.0.1" } else { $uri.Host }
    $port = if ($uri.IsDefaultPort) {
        if ($uri.Scheme -eq "https") { 443 } else { 80 }
    } else {
        $uri.Port
    }

    return @{
        Host = $resolvedHost
        Port = $port
    }
}

$backendProcess = $null
$backendOutLog = Join-Path $projectRoot "target\\e2e-backend.out.log"
$backendErrLog = Join-Path $projectRoot "target\\e2e-backend.err.log"

try {
    $endpoint = Resolve-BaseEndpoint -Url $BaseUrl
    $serverHost = $endpoint.Host
    $serverPort = [int]$endpoint.Port

    # 避免端口被其他进程占用导致“命中旧服务”而回归失真。
    if (Test-TcpPort -HostName $serverHost -Port $serverPort -TimeoutMs 300) {
        throw "端口 $serverPort 已被占用，请先停止占用进程，或传入其它 -BaseUrl（例如 http://127.0.0.1:18081）。"
    }

    Write-Host "[1/5] 打包后端（跳过单测）..."
    mvn -q -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        throw "后端打包失败，退出码：$LASTEXITCODE"
    }

    # 生成稳定的二进制上传样本（1x1 PNG），用于真实 PUT 上传回归
    $uploadFixturePath = Join-Path $projectRoot "target\\e2e-upload.png"
    $uploadFixtureBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO5W8f8AAAAASUVORK5CYII="
    [IO.File]::WriteAllBytes($uploadFixturePath, [Convert]::FromBase64String($uploadFixtureBase64))
    $uploadFixtureSize = (Get-Item -LiteralPath $uploadFixturePath).Length

    $cacheDir = Join-Path $projectRoot "target\\npm-cache"

    Write-Host "[2/5] 准备 Newman ..."
    $newmanCmd = Get-ChildItem -Path (Join-Path $cacheDir "_npx") -Recurse -Filter "newman.cmd" -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName

    if (-not $newmanCmd) {
        # 首次执行通过 npx 拉取 newman，并固定到本地缓存目录
        $env:npm_config_cache = $cacheDir
        npx --yes newman --version | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "初始化 Newman 失败，退出码：$LASTEXITCODE"
        }
        $newmanCmd = Get-ChildItem -Path (Join-Path $cacheDir "_npx") -Recurse -Filter "newman.cmd" |
                Select-Object -First 1 -ExpandProperty FullName
    }

    if (-not $newmanCmd) {
        throw "未找到 newman.cmd，请检查 Node/npm 环境。"
    }

    $collection = (Resolve-Path (Join-Path $projectRoot $CollectionPath)).Path
    $environment = (Resolve-Path (Join-Path $projectRoot $EnvironmentPath)).Path
    $jarPath = (Resolve-Path (Join-Path $projectRoot "target\\community-backend-0.0.1-SNAPSHOT.jar")).Path

    Write-Host "[3/5] 启动后端（jar + dev profile）..."
    if (Test-Path -LiteralPath $backendOutLog) {
        Remove-Item -LiteralPath $backendOutLog -Force
    }
    if (Test-Path -LiteralPath $backendErrLog) {
        Remove-Item -LiteralPath $backendErrLog -Force
    }

    $backendProcess = Start-Process -FilePath "java" `
            -ArgumentList "-jar `"$jarPath`" --spring.profiles.active=dev --server.port=$serverPort" `
            -WorkingDirectory $projectRoot `
            -PassThru `
            -RedirectStandardOutput $backendOutLog `
            -RedirectStandardError $backendErrLog

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep -Seconds 2
        $ok = Test-TcpPort -HostName $serverHost -Port $serverPort -TimeoutMs 1000
        if ($ok) {
            $ready = $true
            break
        }
        if ($backendProcess.HasExited) {
            break
        }
    }

    if (-not $ready) {
        $exitCode = $null
        if ($backendProcess -and $backendProcess.HasExited) {
            $exitCode = $backendProcess.ExitCode
        }
        Write-Host "[错误] 后端未就绪，进程退出码：$exitCode"
        if (Test-Path -LiteralPath $backendOutLog) {
            Write-Host "------ 后端标准输出（最后 120 行）------"
            Get-Content -LiteralPath $backendOutLog -Tail 120
        }
        if (Test-Path -LiteralPath $backendErrLog) {
            Write-Host "------ 后端错误输出（最后 120 行）------"
            Get-Content -LiteralPath $backendErrLog -Tail 120
        }
        exit 2
    }

    Write-Host "[4/5] 执行 Newman 全链路回归..."
    $minioEndpointForAccessUrl = if ($env:MINIO_ENDPOINT) { $env:MINIO_ENDPOINT } else { "http://127.0.0.1:9000" }
    & $newmanCmd run $collection -e $environment --reporters cli `
        --env-var "baseUrl=$BaseUrl" `
        --env-var "uploadFilePath=$uploadFixturePath" `
        --env-var "uploadFileSize=$uploadFixtureSize" `
        --env-var "minioEndpoint=$minioEndpointForAccessUrl"
    $newmanExitCode = $LASTEXITCODE

    if ($newmanExitCode -ne 0) {
        Write-Host "[结果] 回归失败，退出码：$newmanExitCode"
        exit $newmanExitCode
    }

    Write-Host "[结果] 回归通过。"
} finally {
    Write-Host "[5/5] 停止后端..."
    if ($backendProcess -and -not $backendProcess.HasExited) {
        Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
    }
}
