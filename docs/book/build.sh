#!/usr/bin/env sh
set -eu
IMAGE="${TEXLIVE_IMAGE:-texlive/texlive:latest}"

BOOK_DIR=$(pwd)
MOUNT_DIR=$BOOK_DIR
if command -v audenv >/dev/null 2>&1; then
  if command -v cygpath >/dev/null 2>&1; then
    WINDOWS_BOOK_DIR=$(cygpath -w "$BOOK_DIR")
    MOUNT_DIR=$(audenv path --to wsl "$WINDOWS_BOOK_DIR")
  else
    MOUNT_DIR=$(audenv path --to wsl "$BOOK_DIR" 2>/dev/null || printf '%s' "$BOOK_DIR")
  fi
fi

docker run --rm \
  -v "$MOUNT_DIR:/work" \
  -w /work \
  "$IMAGE" \
  latexmk -pdf -interaction=nonstopmode -halt-on-error main.tex
