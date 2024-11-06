#!/bin/bash

# 공통 함수 import
source /home/ubuntu/app/scripts/common.sh

# 디렉토리 존재 확인 및 생성
log "Creating app directory if not exists..."
mkdir -p "$APP_DIR"

# 환경 변수 파일 생성
log "Creating environment file..."
cat > "$APP_DIR/.env" << EOF
APP_DIR=${APP_DIR}
DOCKER_USERNAME=${DOCKER_USERNAME}
REDIS_PASSWORD=${REDIS_PASSWORD}
EOF

# 파일 권한 및 소유권 설정
log "Setting file permissions..."
chown ubuntu:ubuntu "$APP_DIR/.env"
chmod 600 "$APP_DIR/.env"

# 환경 변수 파일 검증
check_environment || exit 1

log "Environment setup completed successfully"
exit 0