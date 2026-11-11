$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$services = @('mm-backend', 'mm-aichatboot', 'mm-gateway')

foreach ($name in $services) {
    $jar = Join-Path $root "$name\target\$name-1.0.0.jar"
    if (-not (Test-Path $jar)) {
        Write-Host "找不到 $jar，请先执行：mvn -DskipTests package" -ForegroundColor Red
        exit 1
    }
}

foreach ($name in $services) {
    $jar = Join-Path $root "$name\target\$name-1.0.0.jar"
    Write-Host "启动 $name ..." -ForegroundColor Cyan
    Start-Process -FilePath 'java' -ArgumentList @('-jar', $jar) -WorkingDirectory $root
    Start-Sleep -Seconds 2
}

Write-Host '三个服务已分别在新窗口启动：mm-gateway(18081) / mm-aichatboot(18082) / mm-backend(18083)' -ForegroundColor Green
