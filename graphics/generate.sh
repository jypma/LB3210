#!/bin/bash
# Exports all SVG files to PNG
set -e

DPI=192

for F in svg/*.svg
do
    inkscape -d $DPI --export-type="png" $F
done
