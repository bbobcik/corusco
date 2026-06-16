param(
    [string] $Output = "build/epub/Swing-with-Corusco.epub",
    [switch] $TtsMetadataExperiment,
    [switch] $SpokenTts,
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
    if ($TtsMetadataExperiment -and -not $PSBoundParameters.ContainsKey("Output")) {
        $Output = "build/epub/Swing-with-Corusco-tts-metadata.epub"
    }
    if ($SpokenTts -and -not $PSBoundParameters.ContainsKey("Output")) {
        $Output = "build/epub/Swing-with-Corusco-spoken.epub"
    }
    if ($TtsMetadataExperiment -and $SpokenTts) {
        throw "Use only one TTS mode at a time: -TtsMetadataExperiment or -SpokenTts."
    }

    $outputPath = Join-Path $bookDir $Output
    $outputDir = Split-Path -Parent $outputPath

    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

    $pandocArgs = @(
        "main.tex",
        "--from=latex",
        "--to=epub3",
        "--resource-path=.",
        "--toc",
        "--toc-depth=2",
        "--split-level=1",
        "--metadata-file=epub/metadata.yaml",
        "--css=epub/epub.css",
        "--lua-filter=epub/latex-to-epub.lua",
        "--fail-if-warnings",
        "--output=$outputPath"
    )

    $previousTtsMode = $env:CORUSCO_EPUB_TTS_MODE
    if ($TtsMetadataExperiment) {
        $env:CORUSCO_EPUB_TTS_MODE = "metadata"
    } elseif ($SpokenTts) {
        $env:CORUSCO_EPUB_TTS_MODE = "replace-code-blocks"
    } else {
        $env:CORUSCO_EPUB_TTS_MODE = $null
    }

    & $pandoc.Source `
        @pandocArgs

    $env:CORUSCO_EPUB_TTS_MODE = $previousTtsMode

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
