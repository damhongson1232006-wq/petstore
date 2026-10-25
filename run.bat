@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if errorlevel 1 (
  echo Khong tim thay Maven. Hay mo project bang NetBeans/IntelliJ hoac cai Maven.
  pause
  exit /b 1
)
mvn clean javafx:run
pause
