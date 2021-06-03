#!/bin/bash
# Exports all SVG files to PNG
set -e

DPI=96

for F in svg/*.svg
do
    inkscape -d $DPI --export-type="png" $F
done

for F in bitmap/*
do
    mogrify -path . -resize "1200x700" $F
done
