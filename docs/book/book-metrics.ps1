param(
    [string] $BookRoot = (Split-Path -Parent $MyInvocation.MyCommand.Path),
    [switch] $ThinOnly,
    [switch] $FailOnUnder
)

$ErrorActionPreference = 'Stop'

$HardMinParagraphs = 55
$HardMinWords = 2500
$HardMinAverageWords = 40
$HardMaxShortRatio = 0.55
$HardMinParasPerHeading = 2.5

$SweetParagraphs = 105
$SweetWords = 6500
$SweetMedianWords = 55
$SweetMaxShortRatio = 0.35
$SweetMinParasPerHeading = 3.5

function Get-ChapterPages {
    param([string] $TocPath)
    $chapters = @()
    if (!(Test-Path $TocPath)) {
        return @{}
    }
    Get-Content $TocPath | ForEach-Object {
        if ($_ -match '\\contentsline \{chapter\}\{\\numberline \{([^}]*)\}([^}]*)\}\{(\d+)\}') {
            $title = ($matches[2] -replace '\\[^ ]+', '' -replace '\{|\}', '').Trim()
            $chapters += [pscustomobject]@{
                Number = $matches[1]
                Title = $title
                Page = [int] $matches[3]
            }
        } elseif ($_ -match '\\contentsline \{chapter\}\{([^{}]+)\}\{(\d+)\}') {
            $title = ($matches[1] -replace '\\[^ ]+', '' -replace '\{|\}', '').Trim()
            $chapters += [pscustomobject]@{
                Number = ''
                Title = $title
                Page = [int] $matches[2]
            }
        }
    }

    $result = @{}
    for ($i = 0; $i -lt $chapters.Count; $i++) {
        $next = if ($i -lt $chapters.Count - 1) { $chapters[$i + 1].Page } else { $null }
        $span = if ($null -ne $next) { $next - $chapters[$i].Page } else { $null }
        $result[$chapters[$i].Title] = $span
    }
    return $result
}

function Get-Median {
    param([int[]] $Values)
    if ($Values.Count -eq 0) {
        return 0
    }
    $sorted = $Values | Sort-Object
    $middle = [math]::Floor($sorted.Count / 2)
    if ($sorted.Count % 2 -eq 1) {
        return $sorted[$middle]
    }
    return [math]::Round(($sorted[$middle - 1] + $sorted[$middle]) / 2.0, 1)
}

function Strip-Tex {
    param([string] $Line)
    $text = $Line
    $text = $text -replace '\\api\{([^}]*)\}', '$1'
    $text = $text -replace '\\termdef\{([^}]*)\}\{([^}]*)\}', '$1 $2'
    $text = $text -replace '\\[a-zA-Z]+\*?(?:\[[^\]]*\])?\{([^{}]*)\}', '$1'
    $text = $text -replace '\\[a-zA-Z]+\*?', ''
    $text = $text -replace '[{}$]', ''
    $text = $text -replace '~', ' '
    $text = $text -replace '---', ' '
    return $text.Trim()
}

function Get-ProseParagraphWordCounts {
    param([string[]] $Lines)
    $skipDepth = 0
    $skipEnvironments = @(
        'lstlisting', 'tabular', 'tabularx', 'longtable', 'figure',
        'center', 'chaptermap', 'exercisebox', 'itemize', 'enumerate'
    )
    $paragraph = New-Object System.Collections.Generic.List[string]
    $counts = New-Object System.Collections.Generic.List[int]

    function Flush-Paragraph {
        if ($paragraph.Count -eq 0) {
            return
        }
        $joined = ($paragraph -join ' ')
        $words = @($joined -split '\s+' | Where-Object { $_ -match '\S' })
        if ($words.Count -gt 0) {
            $counts.Add($words.Count)
        }
        $paragraph.Clear()
    }

    foreach ($line in $Lines) {
        $raw = ($line -replace '(?<!\\)%.*$', '').Trim()

        foreach ($env in $skipEnvironments) {
            if ($raw -match "\\begin\{$env\}") {
                Flush-Paragraph
                $skipDepth++
            }
        }
        if ($skipDepth -gt 0) {
            foreach ($env in $skipEnvironments) {
                if ($raw -match "\\end\{$env\}") {
                    $skipDepth = [Math]::Max(0, $skipDepth - 1)
                }
            }
            continue
        }

        if ($raw -eq '') {
            Flush-Paragraph
            continue
        }
        if ($raw -match '^\\(chapter|section|subsection|subsubsection|paragraph|label|caption|includegraphics|toprule|midrule|bottomrule)\b') {
            Flush-Paragraph
            continue
        }
        if ($raw -match '^\\(begin|end)\b') {
            Flush-Paragraph
            continue
        }
        if ($raw -match '^\\item\b') {
            Flush-Paragraph
            continue
        }
        if ($raw -match '&.*\\\\$') {
            Flush-Paragraph
            continue
        }

        $stripped = Strip-Tex $raw
        if ($stripped -eq '') {
            Flush-Paragraph
            continue
        }
        $paragraph.Add($stripped)
    }
    Flush-Paragraph
    return [int[]] $counts
}

$pagesByTitle = Get-ChapterPages (Join-Path $BookRoot 'main.toc')
$rows = @()
Get-ChildItem (Join-Path $BookRoot 'chapters') -Filter '*.tex' | Sort-Object Name | ForEach-Object {
    $lines = Get-Content $_.FullName
    $content = $lines -join "`n"
    $chapterLine = $lines | Where-Object { $_ -match '^\\chapter\{' } | Select-Object -First 1
    $title = if ($chapterLine -match '^\\chapter\{(.+)\}') { $matches[1] } else { $_.BaseName }
    $pages = if ($pagesByTitle.ContainsKey($title)) { $pagesByTitle[$title] } else { $null }

    $sections = ([regex]::Matches($content, '(?m)^\\section\{')).Count
    $subsections = ([regex]::Matches($content, '(?m)^\\subsection\{')).Count
    $subsub = ([regex]::Matches($content, '(?m)^\\subsubsection\{')).Count
    $paragraphHeadings = ([regex]::Matches($content, '(?m)^\\paragraph\{')).Count
    $numberedHeadings = $sections + $subsections + $subsub
    $counts = Get-ProseParagraphWordCounts $lines
    $wordTotal = ($counts | Measure-Object -Sum).Sum
    if ($null -eq $wordTotal) { $wordTotal = 0 }
    $avg = if ($counts.Count -gt 0) { [math]::Round($wordTotal / $counts.Count, 1) } else { 0 }
    $median = Get-Median $counts
    $short = @($counts | Where-Object { $_ -lt 45 }).Count
    $tiny = @($counts | Where-Object { $_ -lt 25 }).Count
    $shortRatio = if ($counts.Count -gt 0) { [math]::Round($short / $counts.Count, 2) } else { 1 }
    $visibleHeadings = $numberedHeadings + $paragraphHeadings
    $parasPerHeading = if ($visibleHeadings -gt 0) {
        [math]::Round($counts.Count / $visibleHeadings, 1)
    } else {
        $counts.Count
    }

    $status = 'sweet'
    if (($counts.Count -lt $SweetParagraphs) -or ($wordTotal -lt $SweetWords) -or
            ($median -lt $SweetMedianWords) -or ($shortRatio -gt $SweetMaxShortRatio) -or
            ($visibleHeadings -gt 0 -and $parasPerHeading -lt $SweetMinParasPerHeading)) {
        $status = 'minimum'
    }
    if (($counts.Count -lt $HardMinParagraphs) -or ($wordTotal -lt $HardMinWords) -or
            ($avg -lt $HardMinAverageWords)) {
        $status = 'under'
    }
    $flow = if (($shortRatio -gt $HardMaxShortRatio) -or
            ($visibleHeadings -gt 0 -and $parasPerHeading -lt $HardMinParasPerHeading)) {
        'fragmented'
    } else {
        'ok'
    }

    $rows += [pscustomobject]@{
        File = $_.Name
        Status = $status
        Flow = $flow
        Paras = $counts.Count
        Words = [int] $wordTotal
        AvgWords = $avg
        MedianWords = $median
        ShortParas = $short
        ShortRatio = $shortRatio
        TinyParas = $tiny
        Heads = $visibleHeadings
        ParasPerHead = $parasPerHeading
        PdfPages = $pages
        Sections = $sections
        Numbered = $numberedHeadings
        ParaHeads = $paragraphHeadings
        Title = $title
    }
}

$displayRows = if ($ThinOnly) { $rows | Where-Object { $_.Status -eq 'under' } } else { $rows }

function Get-VolumeMark {
    param([string] $Status)
    switch ($Status) {
        'under' { return 'U' }
        'minimum' { return 'M' }
        'sweet' { return 'S' }
        default { return $Status }
    }
}

function Get-FlowMark {
    param([string] $Flow)
    switch ($Flow) {
        'fragmented' { return 'f' }
        default { return $Flow }
    }
}

function Format-AuditValue {
    param($Value)
    if ($null -eq $Value) {
        return ''
    }
    return [string] $Value
}

$format = '{0,-29} {1,1} {2,-2} {3,4} {4,5} {5,5} {6,5} {7,5} {8,3} {9,5} {10,3}'
Write-Output ($format -f 'Ch', 'V', 'F', 'P', 'W', 'Avg', 'Med', 'Short', 'H', 'P/H', 'Pg')
Write-Output ($format -f ('-' * 29), '-', '--', '----', '-----', '-----', '-----', '-----', '---', '-----', '---')
foreach ($row in $displayRows) {
    Write-Output ($format -f
        ($row.File -replace '\.tex$', ''),
        (Get-VolumeMark $row.Status),
        (Get-FlowMark $row.Flow),
        (Format-AuditValue $row.Paras),
        (Format-AuditValue $row.Words),
        (Format-AuditValue $row.AvgWords),
        (Format-AuditValue $row.MedianWords),
        (Format-AuditValue $row.ShortRatio),
        (Format-AuditValue $row.Heads),
        (Format-AuditValue $row.ParasPerHead),
        (Format-AuditValue $row.PdfPages))
}

if ($FailOnUnder -and (@($rows | Where-Object { $_.Status -eq 'under' }).Count -gt 0)) {
    throw 'One or more chapters are under the paragraph-volume floor.'
}
