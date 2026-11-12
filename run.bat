@echo off
chcp 65001 >nul
echo ============================================================
echo   CINEMAX - BUILD VA CHAY PROJECT TREN TOMCAT 10.1
echo ============================================================

REM 1. Thiet lap bien moi truong cho Java va Tomcat (neu chua co)
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if "%CATALINA_HOME%"=="" set "CATALINA_HOME=D:\apache-tomcat-10.1.34\apache-tomcat-10.1.34"
if "%CATALINA_BASE%"=="" set "CATALINA_BASE=%CATALINA_HOME%"

REM 2. Build du an voi Maven
echo [1/3] Dang build package Maven (skip tests)...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Build Maven that bai! Vui long kiem tra lai ma nguon.
    exit /b %ERRORLEVEL%
)

REM 3. Copy file WAR vao webapps cua Tomcat
echo.
echo [2/3] Deploy SWP391-Gr3.war vao Apache Tomcat webapps...
copy /Y "target\SWP391-Gr3.war" "%CATALINA_HOME%\webapps\SWP391-Gr3.war"
if %ERRORLEVEL% NEQ 0 (
    echo [LOI] Khong the copy vao webapps! Kiem tra xem Tomcat co dang chay bi khoa file khong.
    exit /b %ERRORLEVEL%
)

REM 4. Khoi dong Tomcat
echo.
echo [3/3] Dang khoi dong Apache Tomcat 10.1...
echo ------------------------------------------------------------
echo Server dang khoi dong. Mo trinh duyet truy cap:
echo http://localhost:8080/SWP391-Gr3
echo Nhan Ctrl + C de dung Tomcat server.
echo ------------------------------------------------------------
cd /d "%CATALINA_HOME%\bin"
call "%CATALINA_HOME%\bin\catalina.bat" run
