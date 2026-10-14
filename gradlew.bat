@echo off
setlocal
cd /d "%~dp0android"
call "%~dp0android\gradlew.bat" %*
exit /b %ERRORLEVEL%
