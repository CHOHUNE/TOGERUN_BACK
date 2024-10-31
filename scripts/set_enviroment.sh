#!/bin/bash

# 앱 디렉토리 설정
APP_DIR="/home/ubuntu/app"

# 로그 파일 설정
LOG_FILE="/home/ubuntu/app/logs/deployment.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# 디렉토리 존재 확인 및 생성
log "Creating app directory if not exists..."
mkdir -p "$APP_DIR"

# 환경 변수 파일 생성
log "Creating environment file..."
cat > "$APP_DIR/.env" << EOF
DOCKER_USERNAME=${DOCKER_USERNAME}
REDIS_PASSWORD=${REDIS_PASSWORD}
EOF

# 파일 권한 및 소유권 설정
log "Setting file permissions..."
chown ubuntu:ubuntu "$APP_DIR/.env"
chmod 600 "$APP_DIR/.env"

# 환경 변수 파일 확인
log "Verifying .env file creation:"
ls -l "$APP_DIR/.env"

# 환경 변수 파일 내용 확인 (값 제외)
log "Environment file contents (without values):"
grep -v '^#' "$APP_DIR/.env" | cut -d'=' -f1

# 환경 변수 파일 검증
if [ ! -f "$APP_DIR/.env" ]; then
    log "Error: Failed to create .env file"
    exit 1
fi

log "Environment setup completed successfully"
exit 0