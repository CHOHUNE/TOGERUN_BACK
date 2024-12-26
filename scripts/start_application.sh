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

# deployment target 결정
read TARGET_COLOR TARGET_CONTAINER TARGET_PORT <<< $(get_deployment_target)

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
check_container_health $TARGET_CONTAINER $TARGET_PORT 10 15 || {
    log "Error: Container health check failed"
    docker logs $TARGET_CONTAINER
    exit 1
}

log "Deployment completed successfully"
exit 0