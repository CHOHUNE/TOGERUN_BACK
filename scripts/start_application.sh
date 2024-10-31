#!/bin/bash

# 스크립트 실행 디렉토리로 이동
cd /home/ubuntu/app

# 로그 파일 설정
LOG_FILE="/home/ubuntu/app/logs/deployment.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# .env 파일 존재 확인
if [ ! -f ".env" ]; then
    log "Error: .env file not found"
    exit 1
fi

# .env 파일 로드
source .env

# 환경변수 확인
if [ -z "$DOCKER_USERNAME" ]; then
    log "Error: DOCKER_USERNAME is not set"
    exit 1
fi

if [ -z "$REDIS_PASSWORD" ]; then
    log "Error: REDIS_PASSWORD is not set"
    exit 1
fi

# 현재 실행 중인 컨테이너 확인
log "Checking current deployment status..."
BLUE_CONTAINER=$(docker ps -q --filter "name=spring-boot-blue")
GREEN_CONTAINER=$(docker ps -q --filter "name=spring-boot-green")

# 현재 실행 중인 컨테이너 출력
log "Current running containers:"
log "Blue container: ${BLUE_CONTAINER:-none}"
log "Green container: ${GREEN_CONTAINER:-none}"

# Blue/Green 결정
if [ -z "$BLUE_CONTAINER" ]; then
    TARGET_CONTAINER="spring-boot-blue"
    CURRENT_CONTAINER="spring-boot-green"
else
    TARGET_CONTAINER="spring-boot-green"
    CURRENT_CONTAINER="spring-boot-blue"
fi

log "Selected deployment:"
log "Target container: $TARGET_CONTAINER"
log "Current container: $CURRENT_CONTAINER"

# Docker 이미지 풀
log "Pulling latest Docker image..."
if ! docker pull "${DOCKER_USERNAME}"/spring:latest; then
    log "Error: Failed to pull Docker image"
    exit 1
fi

# 기존 컨테이너 정리
log "Cleaning up existing target container..."
docker rm -f $TARGET_CONTAINER || true

# 새 컨테이너 시작 (환경변수 파일 사용)
log "Starting new container: $TARGET_CONTAINER"
if ! docker-compose --env-file .env -f docker-compose.blue-green.yml up -d --force-recreate $TARGET_CONTAINER; then
    log "Error: Failed to start container with docker-compose"
    exit 1
fi

# 컨테이너 시작 확인
log "Verifying container startup..."
if ! docker ps | grep -q $TARGET_CONTAINER; then
    log "Error: Failed to start $TARGET_CONTAINER"
    docker logs $TARGET_CONTAINER
    exit 1
fi

# 헬스체크
log "Performing health check..."
for i in {1..30}; do
    log "Health check attempt $i of 30..."

    if docker exec $TARGET_CONTAINER curl -s "http://localhost:8080/actuator/health" | grep -q "UP"; then
        log "Health check passed! Container is healthy"
        exit 0
    fi

    log "Waiting for container to be healthy..."
    sleep 10
done

log "Error: Container health check failed after 30 attempts"
log "Container logs:"
docker logs $TARGET_CONTAINER
exit 1