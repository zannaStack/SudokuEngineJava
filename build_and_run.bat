@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo ========================================================
echo        COMPILAZIONE E AVVIO SUDOKU ENGINE (JAVAFX)
echo ========================================================

REM Cerca prima il JDK completo in InformaticaSuperiori
set "DESKTOP_JDK=C:\Users\fabio\Desktop\InformaticaSuperiori\Java_Home\jdk-25.0.2\bin"
if exist "!DESKTOP_JDK!\javac.exe" (
    set "JAVAC_CMD=!DESKTOP_JDK!\javac.exe"
    set "JAVA_CMD=!DESKTOP_JDK!\java.exe"
) else (
    where javac >nul 2>nul
    if %errorlevel% equ 0 (
        set JAVAC_CMD=javac
        set JAVA_CMD=java
    ) else (
        set "FALLBACK_BIN=C:\Users\fabio\AppData\Local\Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-epsilon\windows-x64\java-runtime-epsilon\bin"
        set "JAVAC_CMD=!FALLBACK_BIN!\javac.exe"
        set "JAVA_CMD=!FALLBACK_BIN!\java.exe"
    )
)

if not exist "bin" mkdir bin

echo [1/2] Compilazione con JavaFX e SQLite...
"!JAVAC_CMD!" -encoding UTF-8 -cp "lib/*" --module-path "lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -d bin src\it\fabiozanetti\sudoku\model\*.java src\it\fabiozanetti\sudoku\solver\*.java src\it\fabiozanetti\sudoku\generator\*.java src\it\fabiozanetti\sudoku\database\*.java src\it\fabiozanetti\sudoku\service\*.java src\it\fabiozanetti\sudoku\cli\*.java src\it\fabiozanetti\sudoku\gui\*.java src\it\fabiozanetti\sudoku\*.java

if %errorlevel% neq 0 (
    echo [ERRORE] Compilazione fallita!
    pause
    exit /b %errorlevel%
)

echo [2/2] Avvio applicazione JavaFX...
"!JAVA_CMD!" --module-path "lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics --enable-native-access=ALL-UNNAMED -cp "bin;lib/sqlite-jdbc.jar" it.fabiozanetti.sudoku.SudokuApp %*
