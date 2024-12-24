#!/bin/bash
source /home/ubuntu/app/scripts/common.sh
cd $APP_DIR

# 환경 변수 검증
check_environment || exit 1

# Docker network 확인 및 생성
ensure_network

# 현재 실행 중인 컨테이너 확인
get_running_containers

log "Current Blue container: ${BLUE_RUNNING}"
log "Current Green container: ${GREEN_RUNNING}"

# 배포 대상 결정
if [ -z "$BLUE_RUNNING" ] && [ -z "$GREEN_RUNNING" ]; then
    log "No containers running. Deploying blue first."
    TARGET_COLOR="blue"
    TARGET_CONTAINER="spring-boot-blue"
    TARGET_PORT=8081
elif [ -n "$GREEN_RUNNING" ]; then
    log "Green is running. Switching to blue."
    TARGET_COLOR="blue"
    TARGET_CONTAINER="spring-boot-blue"
    TARGET_PORT=8081
else
    log "Blue is running. Switching to green."
    TARGET_COLOR="green"
    TARGET_CONTAINER="spring-boot-green"
    TARGET_PORT=8082
fi

log "Selected deployment: $TARGET_COLOR"

# Docker 이미지 풀
log "Pulling latest Docker image..."
docker pull "${DOCKER_USERNAME}"/spring:latest || exit 1

# 대상 컨테이너 정리
cleanup_container $TARGET_CONTAINER

# 새 컨테이너 시작
log "Starting new containers..."
docker compose --env-file .env -f docker-compose.${TARGET_COLOR}.yml up -d --force-recreate || exit 1

# 컨테이너 헬스체크
check_container_health $TARGET_CONTAINER $TARGET_PORT || {
    log "Error: Container health check failed"
    docker logs $TARGET_CONTAINER
    exit 1
}

log "Deployment completed successfully"
exit 0