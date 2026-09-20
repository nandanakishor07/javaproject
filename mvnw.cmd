@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal
set "DIR=%~dp0"
set "WRAPPER_JAR=%DIR%.mvn\wrapper\maven-wrapper.jar"
java "-Dmaven.multiModuleProjectDirectory=%DIR%." -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
endlocal
