#!/bin/bash

# 스크립트 실행 디렉토리로 이동
cd /home/ubuntu/app

# 로그 파일 설정
LOG_FILE="/home/ubuntu/app/logs/deployment.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# 현재 실행 중인 컨테이너 확인
log "Checking current deployment status..."
CURRENT_CONTAINER=$(docker ps --filter "status=running" --filter "name=spring-boot-blue" -q)

if [ -n "$CURRENT_CONTAINER" ]; then
    NEW_CONTAINER="spring-boot-green"
    OLD_CONTAINER="spring-boot-blue"
else
    NEW_CONTAINER="spring-boot-blue"
    OLD_CONTAINER="spring-boot-green"
fi

log "Current deployment:"
log "New container: $NEW_CONTAINER"
log "Old container: $OLD_CONTAINER"

# nginx 설정 업데이트
log "Updating nginx configuration..."
if ! sed -i "s/server spring-boot-[^:]*:8080/server $NEW_CONTAINER:8080/g" nginx.conf; then
    log "Error: Failed to update nginx configuration"
    exit 1
fi

# nginx 재시작
log "Restarting nginx..."
if ! docker-compose --env-file .env -f docker-compose.blue-green.yml restart nginx; then
    log "Error: Failed to restart nginx"
    exit 1
fi

# 이전 컨테이너 종료 (30초 타임아웃)
log "Stopping old container with grace period..."
docker stop -t 30 $OLD_CONTAINER || true

# 이전 컨테이너 제거
log "Removing old container..."
docker rm $OLD_CONTAINER || true

log "Deployment validation completed successfully"
exit 0