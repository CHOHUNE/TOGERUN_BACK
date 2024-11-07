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
else
    TARGET_COLOR="green"
    TARGET_CONTAINER="spring-boot-green"
    TARGET_PORT=8081
fi

# 최종 헬스체크 (3번 시도)
check_container_health_validate $TARGET_CONTAINER $TARGET_PORT 3 || {
    log "Error: Target container health check failed"
    exit 1
}

# nginx 전환
switch_nginx $TARGET_COLOR || exit 1

log "Deployment validation completed successfully"
exit 0