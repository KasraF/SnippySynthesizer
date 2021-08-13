#!/usr/bin/env bash
set -euo pipefail

CURR_DIR=$PWD;

BENCHMARKS=(
	"cond_benchmarks"
	"frangel_github"
	"loopy_frangel_benchmarks"
	"multiple_spec_benchmarks"
	"old_benchmarks"
	"user_study_candidate_benchmarks"
	"frangel_control"
	"geeksforgeeks"
	"loopy_benchmarks"
	"multivariable_benchmarks"
	"user_study_benchmarks"
	"conala_benchmarks"
	"count_distinct_variants"
)

if [[ -d synthesizer ]]; then
    cd synthesizer;
fi;

if [[ ! -f pom.xml ]]; then
    echo "Please navigate to home (~) or the synthesizer directory (~/synthesizer) before running this script.";
    exit 1;
fi;

echo -n "Compiling... ";
if BUILD_RESULTS=$(mvn clean compile -DskipTests); then
    echo "OK";
	for benchset in ${BENCHMARKS[*]}; do
		echo "Running Benchmark set: " $benchset;
		MAVEN_OPTS="-Xmx8G" mvn exec:java -Dexec.mainClass="edu.ucsd.snippy.BenchmarksCSV" -Dexec.args="$benchset";
	done;
else
    echo "Compiling the synthesizer failed:";
    echo "$BUILD_RESULTS";
fi;

cd $CURR_DIR;
