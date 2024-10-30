#!/bin/bash

# 스크립트 실행 디렉토리로 이동
cd /home/ubuntu/app

# 환경 변수 파일 존재 여부 확인
if [ ! -f .env ]; then
    echo "Error: .env file not found"
    exit 1
fi

# 환경 변수 로드
set -a
source .env
set +a

# 환경 변수 확인
if [ -z "$DOCKER_USERNAME" ]; then
    echo "Error: DOCKER_USERNAME is not set"
    exit 1
fi

if [ -z "$REDIS_PASSWORD" ]; then
    echo "Error: REDIS_PASSWORD is not set"
    exit 1
fi

# 현재 실행 중인 컨테이너 확인
echo "Checking current deployment status..."
BLUE_CONTAINER=$(docker ps -q --filter "name=spring-boot-blue")
GREEN_CONTAINER=$(docker ps -q --filter "name=spring-boot-green")

# 현재 실행 중인 컨테이너 출력
echo "Current running containers:"
echo "Blue container: ${BLUE_CONTAINER:-none}"
echo "Green container: ${GREEN_CONTAINER:-none}"

# Blue/Green 결정
if [ -z "$BLUE_CONTAINER" ]; then
    TARGET_CONTAINER="spring-boot-blue"
    CURRENT_CONTAINER="spring-boot-green"
else
    TARGET_CONTAINER="spring-boot-green"
    CURRENT_CONTAINER="spring-boot-blue"
fi

echo "Selected deployment:"
echo "Target container: $TARGET_CONTAINER"
echo "Current container: $CURRENT_CONTAINER"

# Docker 이미지 풀
echo "Pulling latest Docker image..."
docker pull "${DOCKER_USERNAME}"/spring:latest

# 새 컨테이너 시작
echo "Starting new container: $TARGET_CONTAINER"
docker-compose -f docker-compose.blue-green.yml up -d --force-recreate $TARGET_CONTAINER

# 컨테이너 시작 확인
echo "Verifying container startup..."
if ! docker ps | grep -q $TARGET_CONTAINER; then
    echo "Error: Failed to start $TARGET_CONTAINER"
    docker logs $TARGET_CONTAINER
    exit 1
fi

# 헬스체크
echo "Performing health check..."
for i in {1..30}; do
    echo "Health check attempt $i of 30..."

    if docker exec $TARGET_CONTAINER curl -s "http://localhost:8080/actuator/health" | grep -q "UP"; then
        echo "Health check passed! Container is healthy"

        # 이전 컨테이너가 있다면 로그 출력
        if [ -n "$CURRENT_CONTAINER" ] && docker ps -q --filter "name=$CURRENT_CONTAINER" > /dev/null; then
            echo "Previous container ($CURRENT_CONTAINER) logs:"
            docker logs --tail 100 $CURRENT_CONTAINER
        fi

        exit 0
    fi

    # 실패한 경우 컨테이너 로그 확인
    if [ $i -eq 30 ]; then
        echo "Container logs:"
        docker logs $TARGET_CONTAINER
    fi

    echo "Waiting for container to be healthy..."
    sleep 10
done

echo "Error: Container health check failed after 30 attempts"
echo "Container logs:"
docker logs $TARGET_CONTAINER
exit 1