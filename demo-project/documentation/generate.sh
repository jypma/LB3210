#!/bin/bash
docker run --rm --user $(id -u):$(id -g) -v "$PWD/..:/raml" mattjtodd/raml2html:7.6.0 --validate -i /raml/documentation/demo-api.raml -o /raml/documentation/demo-api.html
