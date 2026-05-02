@echo off
echo =====================================
echo Maven Setup (Temporary for this session)
echo =====================================

:: Add Maven to PATH (temporary)
set PATH=%PATH%;C:\Users\User\maven\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin

echo.
echo Checking Maven Version...
mvn -v

echo.
echo =====================================
echo Checking Java Version
echo =====================================
java -version

echo.
echo =====================================
echo Navigating to Project Directory
echo =====================================
cd /d BIT313-order-processing

echo.
echo =====================================
echo Starting Microservices (New Terminals)
echo =====================================

start cmd /k "cd inventory-service && mvn spring-boot:run"
start cmd /k "cd order-service && mvn spring-boot:run"
start cmd /k "cd payment-service && mvn spring-boot:run"

echo.
echo Waiting for services to start...
timeout /t 15

echo.
echo =====================================
echo Verifying Services
echo =====================================

curl http://localhost:8081/api/inventory/health
curl http://localhost:8082/api/payment/health

echo.
echo =====================================
echo Testing Single Order
echo =====================================

curl -X POST http://localhost:8080/api/orders/process ^
-H "Content-Type: application/json" ^
-d "{\"orderId\":\"ORD-001\",\"productId\":\"PROD-1\",\"quantity\":2,\"totalPrice\":49.99}"

echo.
echo =====================================
echo Phase 3 Stress Testing
echo =====================================

curl -X POST "http://localhost:8080/api/orders/stress-test?count=5000"

echo.
echo =====================================
echo All tasks completed
echo =====================================
pause