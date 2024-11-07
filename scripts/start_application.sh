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


# 현재 실행 중인 컨테이너 확인
get_running_containers

# 현재 실행 중인 컨테이너 출력
log "Current running containers:"
log "Blue nginx: ${BLUE_RUNNING:-none}"
log "Green nginx: ${GREEN_RUNNING:-none}"

# Blue/Green 결정
if [ -z "$BLUE_RUNNING" ]; then
    TARGET_COLOR="blue"
    TARGET_CONTAINER="spring-boot-blue"
    TARGET_PORT=8081
    CURRENT_COLOR="green"
else
    TARGET_COLOR="green"
    TARGET_CONTAINER="spring-boot-green"
    TARGET_PORT=8082
    CURRENT_COLOR="blue"
fi

log "Selected deployment:"
log "Target color: $TARGET_COLOR"
log "Target container: $TARGET_CONTAINER"
log "Target port: $TARGET_PORT"

# Docker 이미지 풀
log "Pulling latest Docker image..."
if ! docker pull "${DOCKER_USERNAME}"/spring:latest; then
    log "Error: Failed to pull Docker image"
    exit 1
fi

# 기존 컨테이너 정리
log "Cleaning up existing target container..."
cleanup_container $TARGET_CONTAINER
cleanup_container nginx-#{TARGET_COLOR}


# 새 컨테이너 시작 (redis 포함 전체 스택)
log "Starting new containers for ${TARGET_COLOR} deployment..."
if ! docker-compose --env-file .env -f docker-compose.${TARGET_COLOR}.yml up -d --force-recreate; then
    log "Error: Failed to start containers with docker-compose"
    exit 1
fi

# Spring Boot 컨테이너 헬스체크
log "Performing health check for Spring Boot container..."
check_container_health $TARGET_CONTAINER $TARGET_PORT || {
    log "Error: Spring Boot container health check failed"
    docker logs $TARGET_CONTAINER
    exit 1
}

# Nginx 컨테이너 헬스체크
log "Performing health check for Nginx container..."
if ! docker exec nginx-${TARGET_COLOR} wget -q --spider http://localhost/health; then
    log "Error: Nginx container health check failed"
    docker logs nginx-${TARGET_COLOR}
    exit 1
fi

log "Deployment completed successfully"
exit 0