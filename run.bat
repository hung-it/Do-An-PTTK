@echo off
echo ========================================
echo CHAY UNG DUNG - He thong Quan ly Cua hang Giay
echo ========================================
echo.

if not exist "PTTK.jar" (
    echo Loi: Khong tim thay file PTTK.jar
    echo Vui long chay build.bat truoc!
    pause
    exit /b 1
)

echo Dang khoi dong ung dung...
java -jar PTTK.jar

pause
