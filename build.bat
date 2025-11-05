@echo off
echo ========================================
echo BUILD JAR FILE - He thong Quan ly Cua hang Giay
echo ========================================
echo.

REM Tao thu muc build neu chua co
if not exist "build" mkdir build

REM Compile tat ca file Java
echo [1/4] Dang compile cac file Java...
dir /s /B src\*.java > sources.txt
javac -encoding UTF-8 -d build -cp "lib/*;src" @sources.txt
if %errorlevel% neq 0 (
    echo COMPILE THAT BAI!
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo Compile thanh cong!
echo.

REM Copy cac file resource neu can
echo [2/4] Copy cac file resource va extract PostgreSQL JAR...
REM Extract PostgreSQL JAR vao build folder
cd build
jar xf ..\lib\postgresql-42.7.8.jar
REM Xoa META-INF cua postgresql de tranh conflict
if exist "META-INF\*.SF" del /Q META-INF\*.SF
if exist "META-INF\*.RSA" del /Q META-INF\*.RSA
if exist "META-INF\*.DSA" del /Q META-INF\*.DSA
cd ..
echo.

REM Tao JAR file voi manifest
echo [3/4] Dang tao file JAR...
cd build
jar cfm ..\PTTK.jar ..\MANIFEST.MF *
cd ..
if %errorlevel% neq 0 (
    echo TAO JAR THAT BAI!
    pause
    exit /b 1
)
echo Tao JAR thanh cong!
echo.

REM Kiem tra ket qua
echo [4/4] Kiem tra file JAR...
if exist "PTTK.jar" (
    echo.
    echo ========================================
    echo BUILD THANH CONG!
    echo File JAR: PTTK.jar
    echo ========================================
    echo.
    echo De chay ung dung:
    echo   java -jar PTTK.jar
    echo.
) else (
    echo BUILD THAT BAI - Khong tim thay file JAR!
)

pause
