@echo off
setlocal
echo Stopping Tomcat 10...
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "CATALINA_HOME=C:\apache-tomcat-10.1.60\apache-tomcat-10.1.60"
cd /d "%CATALINA_HOME%\bin"
call shutdown.bat >nul 2>&1
timeout /t 1 /nobreak >nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080 "') do (
    taskkill /f /pid %%a >nul 2>&1
)
echo Tomcat stopped.

