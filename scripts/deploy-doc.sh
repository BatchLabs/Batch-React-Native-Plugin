#!/bin/sh
set -e

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
