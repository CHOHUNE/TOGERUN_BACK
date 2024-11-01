#!/bin/bash

# 초기 로깅 함수 (common.sh 없을 경우 대비)
init_log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 기본 디렉토리 생성
APP_DIR="/home/ubuntu/app"
mkdir -p "$APP_DIR/scripts"

# 현재 스크립트가 있는 디렉토리 경로 가져오기
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# 프로젝트 루트 디렉토리 (scripts 폴더의 상위 디렉토리)
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# common.sh가 없으면 복사
if [ ! -f "$APP_DIR/scripts/common.sh" ]; then
    init_log "Copying common.sh from project..."
    cp "$PROJECT_ROOT/scripts/common.sh" "$APP_DIR/scripts/common.sh"
    chmod 755 "$APP_DIR/scripts/common.sh"
fi

# 이후 common.sh import
source "$APP_DIR/scripts/common.sh"

# 디렉토리 생성
log "Creating required directories..."
mkdir -p "$APP_DIR/scripts"
mkdir -p "$APP_DIR/docker"
mkdir -p "$APP_DIR/logs"
mkdir -p "$APP_DIR/certbot/conf"
mkdir -p "$APP_DIR/certbot/www"
mkdir -p "$APP_DIR/ssl"

# 권한 설정
log "Setting directory permissions..."
chown -R ubuntu:ubuntu "$APP_DIR"
chmod 755 "$APP_DIR/scripts"
chmod 755 "$APP_DIR/docker"
chmod 755 "$APP_DIR/logs"