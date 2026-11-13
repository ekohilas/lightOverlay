#!/bin/bash
# Tests the torch layouts and regenerates docs/layouts/*.png from the mod's own layout code.
# Needs a JDK and Python 3 with Pillow. No game jar needed: the layout classes are plain Java.
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$HERE/../.."
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -nowarn -d "$OUT" "$ROOT"/src/main/java/lightOverlay/autotorch/{TorchLayout,Lattice,QuadrantLayout,LayoutType}.java "$HERE"/LayoutTest.java "$HERE"/Dump.java
java -cp "$OUT" LayoutTest
java -cp "$OUT" Dump "$OUT"
python3 "$HERE/draw.py" "$OUT"
echo "Diagrams written to docs/layouts/"
