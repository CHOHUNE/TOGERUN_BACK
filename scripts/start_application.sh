#!/bin/bash

# 공통 함수 import
source /home/ubuntu/app/scripts/common.sh
export APP_DIR="/home/ubuntu/app"
# 작업 디렉토리 이동
cd $APP_DIR

# 환경 변수 검증
check_environment || exit 1

# Docker network 확인 및 생성
ensure_network

# Redis 컨테이너 확인 및 시작
log "Checking Redis container..."
if ! docker ps | grep -q "redis_boot"; then
    log "Starting Redis container..."
    if ! docker-compose --env-file .env -f docker-compose.blue-green.yml up -d redis; then
        log "Error: Failed to start Redis container"
        exit 1
    fi
fi

# Nginx 컨테이너 확인 및 시작
log "Checking Nginx container..."
if ! docker ps | grep -q "nginx"; then
    log "Starting Nginx container..."
    if ! docker-compose --env-file .env -f docker-compose.blue-green.yml up -d nginx certbot; then
        log "Error: Failed to start Nginx container"
        exit 1
    fi
fi

# 현재 실행 중인 컨테이너 확인
get_running_containers

# 현재 실행 중인 컨테이너 출력
log "Current running containers:"
log "Blue container: ${BLUE_RUNNING:-none}"
log "Green container: ${GREEN_RUNNING:-none}"

# Blue/Green 결정
if [ -z "$BLUE_RUNNING" ]; then
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
cleanup_container $TARGET_CONTAINER

# 새 컨테이너 시작
log "Starting new container: $TARGET_CONTAINER"
if ! docker-compose --env-file .env -f docker-compose.blue-green.yml up -d --force-recreate $TARGET_CONTAINER; then
    log "Error: Failed to start container with docker-compose"
    exit 1
fi

# 헬스체크
check_container_health $TARGET_CONTAINER || {
    log "Error: Container health check failed"
    docker logs $TARGET_CONTAINER
    exit 1
}

log "Container started and healthy"
exit 0