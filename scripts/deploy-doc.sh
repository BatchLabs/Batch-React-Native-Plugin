#!/bin/sh
set -e

# Git config
git config --global user.email "41898282+github-actions[bot]@users.noreply.github.com"
git config --global user.name "github-actions"

# Copy generated documentation
cp -r docs/. ../temp-doc

# Checkout gh-pages branch
git fetch origin gh-pages
git checkout gh-pages

# Discard all
git reset --hard
git clean -fxd

# Update documentation
cp -a ../temp-doc/. .

# Commit & push documentation
git add -A
git commit -m "doc: updates documentation"
git push origin gh-pages
