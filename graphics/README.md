# Graphics project
This project contains graphics and illustrations for the LB3210 course.

## How to use

1. Set the Environment variable GRAPHVIZ_DOT to the 
   executable dot file of your Graphviz installation.
   See [Graphviz website](https://graphviz.org/) for download.
   On Windows the GRAPHVIZ_DOT env variable is something like:                 
   _C:\Program Files\Graphviz\bin\dot.exe_.
2. Add plant-uml diagram files to _./src/main/plantuml_
3. Generate SVG files by running Maven:
   _mvn clean com.github.jmdesprez:plantuml-maven-plugin:generate_
4. SVG files generated from plant-uml text files can be found in ./target/plantuml

## Generating PNG from SVG files

1. Install Inkscape
2. Run `./generate.sh`
