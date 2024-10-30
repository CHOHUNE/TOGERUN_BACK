#!/bin/bash

# 앱 디렉토리 확인
APP_DIR="/home/ubuntu/app"
if [ ! -d "$APP_DIR" ]; then
    echo "Error: Application directory not found"
    exit 1
fi

# 환경 변수 파일 생성
ENV_FILE="$APP_DIR/.env"

# 환경 변수 파일 생성
cat > "$ENV_FILE" << EOF
DOCKER_USERNAME=$DOCKER_USERNAME
REDIS_PASSWORD=$REDIS_PASSWORD
EOF

# 파일 권한 설정
chmod 600 "$ENV_FILE"
chown ubuntu:ubuntu "$ENV_FILE"

# 환경 변수 파일 확인
if [ -f "$ENV_FILE" ]; then
    echo "Environment file created successfully at $ENV_FILE"
    echo "File permissions:"
    ls -l "$ENV_FILE"
else
    echo "Error: Failed to create environment file"
    exit 1
fi