# Проверка: запущен ли локальный backend (порты 3001, 3002, 3003)
# Сначала запустите ОТКРЫТЬ_ЛОКАЛЬНЫЙ_СЕРВЕР.bat, затем: .\ПРОВЕРИТЬ_СЕРВЕР.ps1

$checks = @(
    @{ Port = 3001; Name = "Auth";    Uri = "http://localhost:3001/" },
    @{ Port = 3002; Name = "Support"; Uri = "http://localhost:3002/support/tickets" },
    @{ Port = 3003; Name = "Scan";    Uri = "http://localhost:3003/" }
)

foreach ($c in $checks) {
    try {
        $null = Invoke-WebRequest -Uri $c.Uri -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        Write-Host "[OK] $($c.Name) (port $($c.Port))" -ForegroundColor Green
    } catch {
        Write-Host "[--] $($c.Name) (port $($c.Port)): нет ответа. Запустите ОТКРЫТЬ_ЛОКАЛЬНЫЙ_СЕРВЕР.bat" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Если все три [OK] — тестируйте вход и тех. поддержку в приложении." -ForegroundColor Cyan
