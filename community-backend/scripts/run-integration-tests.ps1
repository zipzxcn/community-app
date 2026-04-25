$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path
Set-Location $projectRoot

Write-Host "[1/2] 执行 JUnit 集成测试（integration profile）..."
mvn -q -P integration verify
if ($LASTEXITCODE -ne 0) {
    throw "JUnit 集成测试失败，退出码：$LASTEXITCODE"
}

Write-Host "[2/2] 集成测试执行完成。"
Write-Host "结果文件：target\\failsafe-reports\\TEST-*.xml"
