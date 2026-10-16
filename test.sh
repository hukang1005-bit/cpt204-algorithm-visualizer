#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
javac --release 17 -encoding UTF-8 -cp build/classes -d build/classes tests/AlgorithmChecks.java
java -cp build/classes AlgorithmChecks
