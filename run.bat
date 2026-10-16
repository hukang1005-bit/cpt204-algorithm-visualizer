@echo off
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
powershell -NoProfile -Command "Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { [char]34 + (Resolve-Path -Relative $_.FullName) + [char]34 } | Set-Content -Encoding ASCII build/sources.txt"
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt
if errorlevel 1 exit /b 1
java -cp build/classes Main %*
