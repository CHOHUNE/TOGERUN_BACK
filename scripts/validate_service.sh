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

# deployment target 결정
read NEW_COLOR NEW_CONTAINER NEW_PORT <<< $(get_deployment_target)
CURRENT_COLOR=$([ "$NEW_COLOR" = "blue" ] && echo "green" || echo "blue")

log "Current active environment : $CURRENT_COLOR"
log "NEW environment to validate : $NEW_COLOR"

# 새로 배포된 환경 헬스 체크
check_container_health $NEW_CONTAINER $NEW_PORT 10 10 || {
    log "Error: New container health check failed"
    exit 1
}

# nginx 전환
if switch_nginx $NEW_COLOR; then
    log "Successfully switched to $NEW_COLOR"
#    cleanup_container "spring-boot-$CURRENT_COLOR"
else
    log "Error: Failed to switch nginx to $NEW_COLOR"
    switch_nginx $CURRENT_COLOR # 실패시 롤백
    exit 1
fi

# 최종 헬스체크
check_container_health $NEW_CONTAINER $NEW_PORT 10 10 || {
    log "Error: Target container health check failed"
    exit 1
}

log "Successfully switched traffic to $NEW_COLOR environment"
exit 0