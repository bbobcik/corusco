param(
    [string] $Output = "build/epub/Swing-with-Corusco.epub",
    [switch] $SkipEpubCheck
)

$ErrorActionPreference = "Stop"

$bookDir = $PSScriptRoot
if (-not $bookDir) {
    $bookDir = Split-Path -Parent $MyInvocation.MyCommand.Path
}

Push-Location $bookDir
try {
    $pandoc = Get-Command pandoc -ErrorAction Stop
    $outputPath = Join-Path $bookDir $Output
    $outputDir = Split-Path -Parent $outputPath

    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

    & $pandoc.Source `
        "main.tex" `
        "--from=latex" `
        "--to=epub3" `
        "--resource-path=." `
        "--toc" `
        "--toc-depth=2" `
        "--split-level=1" `
        "--metadata-file=epub/metadata.yaml" `
        "--css=epub/epub.css" `
        "--lua-filter=epub/latex-to-epub.lua" `
        "--fail-if-warnings" `
        "--output=$outputPath"

    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    if (-not (Test-Path -LiteralPath $outputPath)) {
        throw "Pandoc completed but did not create $outputPath"
    }

    $epub = Get-Item -LiteralPath $outputPath
    Write-Output "ok task=book-epub output=$($epub.FullName) size=$($epub.Length)"

    if (-not $SkipEpubCheck) {
        $epubCheck = Get-Command epubcheck -ErrorAction SilentlyContinue
        if ($epubCheck) {
            & $epubCheck.Source $epub.FullName
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
            Write-Output "ok task=epubcheck"
        } else {
            Write-Output "skip task=epubcheck reason=not-found"
        }
    }
} finally {
    Pop-Location
}
