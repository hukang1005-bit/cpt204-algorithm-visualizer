#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt
jar --create --file build/algorithm-visualizer.jar --main-class Main -C build/classes .
