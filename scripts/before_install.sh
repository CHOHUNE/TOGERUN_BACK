#!/bin/bash

# 초기 로깅 함수 (common.sh 없을 경우 대비)
init_log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 기본 디렉토리 생성
APP_DIR="/home/ubuntu/app"
mkdir -p "$APP_DIR/scripts"

# common.sh가 없으면 생성
if [ ! -f "$APP_DIR/scripts/common.sh" ]; then
    init_log "Creating common.sh..."
    cp /path/to/source/common.sh "$APP_DIR/scripts/common.sh"
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