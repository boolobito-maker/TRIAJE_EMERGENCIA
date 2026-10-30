@echo off
if not exist out mkdir out
javac -encoding UTF-8 -d out src\main\java\*.java
java -cp out fmrPrin
pause
