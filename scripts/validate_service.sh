#!/bin/bash
source /home/ubuntu/app/scripts/common.sh
cd $APP_DIR

# 현재 실행 중인 컨테이너 확인
get_running_containers

log "Current running containers:"
log "Blue: ${BLUE_RUNNING:-none}"
log "Green: ${GREEN_RUNNING:-none}"

# 대상 컨테이너 결정
if [ -n "$BLUE_RUNNING" ]; then
   TARGET_COLOR="blue"
   TARGET_CONTAINER="spring-boot-blue"
   TARGET_PORT=8081
   INACTIVE_COLOR="green"
   INACTIVE_CONTAINER="spring-boot-green"
else
   TARGET_COLOR="green"
   TARGET_CONTAINER="spring-boot-green"
   TARGET_PORT=8082
   INACTIVE_COLOR="blue"
   INACTIVE_CONTAINER="spring-boot-blue"
fi

# nginx 전환
switch_nginx $TARGET_COLOR || exit 1

# 최종 헬스체크 (10번 시도)
check_container_health_validate $TARGET_CONTAINER $TARGET_PORT 10 || {
   log "Error: Target container health check failed"
   exit 1
}

# 비활성 환경 업데이트
log "Pulling latest image for inactive $INACTIVE_COLOR environment..."
docker pull "${DOCKER_USERNAME}"/spring:latest

log "Cleaning up and updating inactive $INACTIVE_COLOR container..."
cleanup_container $INACTIVE_CONTAINER
docker compose --env-file .env -f docker-compose.${INACTIVE_COLOR}.yml up -d --force-recreate

sleep 30

log "Deployment v
alidation completed successfully"
exit 0