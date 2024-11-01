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
    TARGET_CONTAINER="spring-boot-blue"
    OLD_CONTAINER="spring-boot-green"
else
    TARGET_CONTAINER="spring-boot-green"
    OLD_CONTAINER="spring-boot-blue"
fi

# 최종 헬스체크 (3번 시도)
check_container_health $TARGET_CONTAINER 3 || {
    log "Error: Target container health check failed"
    exit 1
}

# nginx 설정 업데이트
update_nginx_config $TARGET_CONTAINER || exit 1

# nginx 설정 리로드
reload_nginx || exit 1

# 이전 컨테이너 정리
if [ -n "$OLD_CONTAINER" ]; then
    cleanup_container $OLD_CONTAINER
fi

log "Deployment validation completed successfully"
exit 0