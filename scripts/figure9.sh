#!/usr/bin/env bash
set -euo pipefail

echo -n "Compiling... ";
if BUILD_RESULTS=$(mvn clean compile -DskipTests); then
    echo "OK";
    mvn exec:java -Dexec.mainClass="edu.ucsd.snippy.IterSelectionBenchmarks";
else
    echo "Compiling the synthesizer failed:";
    echo "$BUILD_RESULTS";
fi;
