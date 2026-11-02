@echo off
setlocal
echo ====================================================
echo  Starting CineMax (SWP391) on Tomcat 10
echo ====================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "CATALINA_HOME=C:\apache-tomcat-10.1.60\apache-tomcat-10.1.60"
set "MAVEN_BIN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

echo [1/2] Building webapp (war:exploded)...
call "%MAVEN_BIN%" compile war:exploded
if errorlevel 1 (
    echo [ERROR] Build failed. Tomcat not started.
    exit /b 1
)

echo [2/2] Starting Tomcat 10...
cd /d "%CATALINA_HOME%\bin"
call startup.bat

echo ====================================================
echo  Server is running!
echo  Open browser: http://localhost:8080/SWP391/
echo ====================================================

