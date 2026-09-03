# 用法（PowerShell）：
#   .\set-env.ps1            # 仅导出环境变量到当前 shell
#   .\set-env.ps1 -Run       # 导出后直接 mvn spring-boot:run
# 注意：必须用 "." 前缀 dot-source，否则变量只在子进程生效。

param([switch]$Run)

$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
  Write-Error "未找到 .env，请先复制 .env.example 为 .env 并填入真实值"
  exit 1
}

Get-Content $envFile | ForEach-Object {
  $line = $_.Trim()
  if (-not $line -or $line.StartsWith("#")) { return }
  $idx = $line.IndexOf("=")
  if ($idx -le 0) { return }
  $key = $line.Substring(0, $idx).Trim()
  $val = $line.Substring($idx + 1).Trim()
  Set-Item -Path ("Env:" + $key) -Value $val
  Write-Host "已设置 $key" -ForegroundColor DarkGray
}

if ($Run) {
  Push-Location (Join-Path $PSScriptRoot "server")
  mvn spring-boot:run
  Pop-Location
}
