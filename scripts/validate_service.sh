#!/bin/bash
source /home/ubuntu/app/scripts/common.sh
cd "$APP_DIR" || {
                  echo "Error: Failed to change directory to $APP_DIR"
                  exit 1
                  }

# 현재 실행 중인 컨테이너 확인
get_running_containers

log "Current running containers:"
log "Blue: ${BLUE_RUNNING:-none}"
log "Green: ${GREEN_RUNNING:-none}"

# 대상 컨테이너 결정
# 환경 결정
if [ -n "$BLUE_RUNNING" ]; then
   CURRENT_COLOR="green"
   NEW_COLOR="blue"
   NEW_CONTAINER="spring-boot-blue"
   NEW_PORT=8081
else
   CURRENT_COLOR="blue"
   NEW_COLOR="green"
   NEW_CONTAINER="spring-boot-green"
   NEW_PORT=8082
fi

log "Current active environment : $CURRENT_COLOR"
log "NEW environment to validate : $NEW_COLOR"

# 새로 배포된 환경 헬스 체크
check_container_health_validate $NEW_CONTAINER $NEW_PORT 10 ||{
  log "Error: New container health check failed"
  exit 1
}

# nginx 전환
switch_nginx $NEW_COLOR || {
    log "Error: Failed to switch nginx to $NEW_COLOR"
    switch_nginx $CURRENT_COLOR # 실패시 롤백
    exit 1
}

# 최종 헬스체크 (10번 시도)
check_container_health_validate $NEW_CONTAINER $NEW_PORT 10 || {
   log "Error: Target container health check failed"
   exit 1
}

log "Successfully switched traffic to $INACTIVE_COLOR environment"

exit 0