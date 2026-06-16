# Swing with Corusco

This is the LaTeX source project for the A4 PDF book **Swing with Corusco**.

## Build

Recommended build command, using the Docker-based TeX toolchain:

```sh
./build.sh
```

The script uses AudEnv path conversion when available so WSL-backed Docker can mount the book directory correctly. It uses `texlive/texlive:latest` by default. Override it with `TEXLIVE_IMAGE` when a different TeX Live image is required:

```sh
TEXLIVE_IMAGE=texlive/texlive:latest ./build.sh
```

The generated PDF is `main.pdf`.

## A5 PDF edition

The A5 edition uses the same chapter sources as the A4 book, but it has its own
LaTeX entry point with A5 page geometry, smaller heading scale, tighter boxes,
and listing/table settings chosen for the narrower measure. It is not produced
by scaling the A4 PDF.

```sh
./build-a5.sh
```

The generated PDF is `main-a5.pdf`.

## EPUB build

The EPUB edition is generated from the same LaTeX source with Pandoc. It is a
reflowable EPUB 3 output tuned for mobile readers such as ReadEra, so it favors
reader-controlled fonts, margins, wrapping, and image scaling over exact A4 PDF
layout.

```powershell
powershell -ExecutionPolicy Bypass -File .\build-epub.ps1
```

The script switches to the `docs/book` directory before running Pandoc so
`\include{...}` paths, figures, metadata, CSS, and Lua filters resolve
consistently. The generated EPUB is
`build/epub/Swing-with-Corusco.epub`. If `epubcheck` is available on `PATH`, the
script runs it after generation; otherwise that validation step is skipped.

To test whether a reader's Android TTS path honors hidden accessibility hints,
build the experimental TTS-metadata variant:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-epub.ps1 -TtsMetadataExperiment
```

This writes `build/epub/Swing-with-Corusco-tts-metadata.epub`. The variant adds
visually hidden short summaries before code blocks and marks the visible code
blocks with accessibility metadata intended to discourage TTS from reading the
raw code. Support depends on the EPUB reader; the normal EPUB output is
unchanged.

If the reader still speaks the raw code, build the spoken TTS variant instead:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-epub.ps1 -SpokenTts
```

This writes `build/epub/Swing-with-Corusco-spoken.epub`. It physically replaces
code blocks with short visible summaries, which is more intrusive for visual
reading but avoids leaving Java source text for simple reader-driven TTS engines
to speak.

## Chapter volume audit

Use the paragraph-volume audit after substantial drafting:

```powershell
powershell -ExecutionPolicy Bypass -File .\book-metrics.ps1
```

The audit keeps TeX page spans as useful context in the compact `Pg` column, but
draft status is based on prose paragraphs because boxes, figures, listings, and
floats make page counts a poor editing signal. The hard floor for an ordinary
chapter is 55 prose paragraphs, 2500 prose words, average paragraph length of at
least 40 words, and no more than 55 percent short prose paragraphs. The intended
sweet spot is 105 or more prose paragraphs, 6500 or more prose words, median
paragraph length of at least 55 words, no more than 35 percent short prose
paragraphs, and at least 3.5 prose paragraphs per visible heading.

Run a failing audit when a gate is useful:

```powershell
powershell -ExecutionPolicy Bypass -File .\book-metrics.ps1 -FailOnUnder
```

The compact table uses `V` for volume status (`U` = `under`, `M` =
`minimum`, `S` = `sweet`), `F` for flow (`f` = `fragmented`), `P` for prose
paragraphs, `W` for prose words, `Avg` and `Med` for paragraph size, `Short`
for the ratio of prose paragraphs shorter than 45 words, `H` for visible
headings, `P/H` for prose paragraphs per visible heading, and `Pg` for TeX page
span.

`under` means the chapter is still below the hard floor. The floor represents a
defendable draft chapter: 55 prose paragraphs, 2500 prose words, average
paragraph length of at least 40 words. `minimum` means it has cleared the floor
but should normally continue toward the sweet spot. `sweet` means its paragraph
volume is in the desired range; it still needs normal technical review,
examples, screenshots, and TeX inspection. The separate `Flow` column reports
`fragmented` when more than 55 percent of prose paragraphs are short, or when
the chapter has fewer than 2.5 prose paragraphs per visible heading. That is a
rewrite/consolidation signal, not the same thing as being below the volume
floor.

## Expected toolchain

The PDF source is built with pdfLaTeX via Dockerized `latexmk`. It uses common TeX Live packages including `libertine`, `inconsolata`, `microtype`, `geometry`, `fancyhdr`, `titlesec`, `booktabs`, `tabularx`, `tcolorbox`, `listings`, `hyperref`, `xurl`, `epigraph`, `needspace`, `emptypage`, and `nowidow`. The EPUB source is built with Pandoc and EPUB-specific metadata, CSS, and Lua filtering under `epub/`.

## Project layout

- `main.tex` - document preamble and chapter includes.
- `main-a5.tex` - A5 PDF preamble and the same chapter includes.
- `build-a5.sh` - Dockerized A5 PDF build entry point.
- `build-epub.ps1` - Pandoc EPUB build entry point.
- `epub/` - EPUB metadata, mobile-first CSS, and LaTeX-to-EPUB filter.
- `frontmatter/` - title page and preface.
- `chapters/` - the foundation, Swing mechanics, Corusco, generation, and packaging chapter source files.
- `examples.tex` - central macros for companion example source paths.
- `backmatter/` - selected references.

## Notes

The document is an original technical synthesis for Java Swing developers using JDK 25+, MigLayout, FlatLaf, and Corusco. Java, Swing, OpenJDK, MigLayout, FlatLaf, and Corusco names are used descriptively. Consult official API documentation for normative contracts.
