@echo off
setlocal

if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\PyCharm 2025.3.1\jbr" (
        set "JAVA_HOME=C:\Program Files\JetBrains\PyCharm 2025.3.1\jbr"
    )
)

if not defined ANDROID_HOME (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    ) else if exist "C:\Users\User\AppData\Local\Android\Sdk" (
        set "ANDROID_HOME=C:\Users\User\AppData\Local\Android\Sdk"
    )
)

set GRADLE_BIN="%LOCALAPPDATA%\Android\gradle-8.7\bin\gradle.bat"
if not exist %GRADLE_BIN% (
    set GRADLE_BIN="C:\Users\User\AppData\Local\Android\gradle-8.7\bin\gradle.bat"
)

call %GRADLE_BIN% %*
exit /b %ERRORLEVEL%
