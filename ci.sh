# !/bin/bash
set -e  # stop on error

echo -e "\n=== Step 1: Build JAR ===\n"
mvn clean package -DskipTests

echo -e "\n=== Step 2: Dependency scan (.jar) ===\n"
trivy fs target/*.jar \
    --severity HIGH,CRITICAL \
    --exit-code 1 \
    --ignore-unfixed

echo -e "\n=== Step 3: Build Docker image ===\n"
docker build -t myserver:latest .

echo -e "\n=== Step 4: Image scan ===\n"
trivy image myserver:latest \
    --severity HIGH,CRITICAL \
    --exit-code 1 \
    --ignore-unfixed

echo -e "\n=== Done ===\n"

# docker run --rm \                            
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   -v ./:/app \     
#   -w /app \         
#   ci-pipeline:latest \
#   bash ci.sh