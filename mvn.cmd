@echo off
call "%~dp0mvnw.cmd" %*
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
