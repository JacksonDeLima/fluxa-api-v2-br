param(
    [int]$Porta = 8080
)

function Get-PortaInfo {
    param(
        [int]$PortaAlvo
    )

    try {
        $conexoes = Get-NetTCPConnection -LocalPort $PortaAlvo -State Listen -ErrorAction Stop
    } catch {
        $linhas = netstat -ano -p tcp | Select-String -Pattern "LISTENING"
        $conexoes = foreach ($linha in $linhas) {
            $colunas = ($linha.Line -split "\s+") | Where-Object { $_ }
            if ($colunas.Count -ge 5) {
                $enderecoLocal = $colunas[1]
                $pid = [int]$colunas[4]
                $portaLinha = [int]($enderecoLocal.Split(":")[-1])

                if ($portaLinha -eq $PortaAlvo) {
                    [pscustomobject]@{
                        LocalAddress  = $enderecoLocal.Substring(0, $enderecoLocal.LastIndexOf(":"))
                        LocalPort     = $portaLinha
                        OwningProcess = $pid
                    }
                }
            }
        }
    }

    return @($conexoes)
}

$conexoes = Get-PortaInfo -PortaAlvo $Porta

if (-not $conexoes -or $conexoes.Count -eq 0) {
    Write-Host "Nenhum processo escutando na porta $Porta."
    exit 0
}

foreach ($conexao in $conexoes) {
    $processoId = $conexao.OwningProcess
    $processo = Get-Process -Id $processoId -ErrorAction SilentlyContinue
    $processoCim = Get-CimInstance Win32_Process -Filter "ProcessId = $processoId" -ErrorAction SilentlyContinue

    [pscustomobject]@{
        Porta        = $Porta
        Endereco     = $conexao.LocalAddress
        PID          = $processoId
        Processo     = if ($processo) { $processo.ProcessName } else { "desconhecido" }
        Caminho      = if ($processo) { $processo.Path } else { $null }
        Comando      = if ($processoCim) { $processoCim.CommandLine } else { $null }
        IniciadoEm   = if ($processo) { $processo.StartTime } else { $null }
    } | Format-List
}
