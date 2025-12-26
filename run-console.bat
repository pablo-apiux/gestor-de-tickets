@echo off
echo 🎫 Compilando interfaz de consola...
mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error en compilación
    pause
    exit /b 1
)

echo ✅ Ejecutando interfaz de consola...
echo.
mvn exec:java -Dexec.mainClass="com.example.ticketero.cli.TicketeroConsoleApp" -Dexec.classpathScope=compile -q