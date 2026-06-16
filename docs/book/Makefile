.PHONY: all a5 clean

all:
	latexmk -pdf -interaction=nonstopmode -halt-on-error main.tex

a5:
	latexmk -pdf -interaction=nonstopmode -halt-on-error -jobname=main-a5 main-a5.tex

clean:
	latexmk -C
