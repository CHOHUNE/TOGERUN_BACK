#!/bin/bash
cd /home/ubuntu/app

# 현재 실행 중인 포트 확인 (기본값 8080)
CURRENT_PORT=$(docker ps -q --filter "name=spring-boot-blue" | wc -l)
echo "Current port: $CURRENT_PORT"

# Blue/Green 결정
if [ $CURRENT_PORT -eq 0 ]; then
    TARGET_PORT=8080
    CONTAINER_NAME=spring-boot-blue
    NETWORK_ALIAS=spring-boot-blue
else
    TARGET_PORT=8081
    CONTAINER_NAME=spring-boot-green
    NETWORK_ALIAS=spring-boot-green
fi

echo "Target port: $TARGET_PORT"

# 새 컨테이너 시작
docker-compose -f docker-compose.blue-green.yml up -d --build $CONTAINER_NAME

# 헬스체크
for i in {1..30}; do
    if curl -s "http://localhost:$TARGET_PORT/actuator/health" | grep "UP"; then
        echo "New container is healthy"
        exit 0
    fi
    echo "Waiting for container to be healthy..."
    sleep 10
done

echo "Container health check failed"
exit 1