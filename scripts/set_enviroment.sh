#!/bin/bash

# 공통 함수 import
source /home/ubuntu/app/scripts/common.sh

# 디렉토리 존재 확인 및 생성
log "Creating app directory if not exists..."
mkdir -p "$APP_DIR"

# 환경 변수 파일 생성
log "Creating environment file..."
cat > "$APP_DIR/.env" << EOF
DOCKER_USERNAME=${DOCKER_USERNAME}
REDIS_PASSWORD=${REDIS_PASSWORD}
SLACK_WEBHOOK_URL=${SLACK_WEBHOOK_URL}
EOF

# 파일 권한 및 소유권 설정
log "Setting file permissions..."
chown ubuntu:ubuntu "$APP_DIR/.env"
chmod 600 "$APP_DIR/.env"

# 환경 변수 안전하게 로드
log "Loading environment variables safely..."
while IFS= read -r line; do
    # 주석이나 빈 줄 무시
    if [[ ! $line =~ ^# ]] && [[ -n $line ]]; then
        # 변수 이름만 추출하여 로그에 기록
        var_name=$(echo "$line" | cut -d'=' -f1)
        log "Exporting variable: $var_name"
        export "$line"
    fi
done < "$APP_DIR/.env"

# 환경 변수 파일 검증
log "Validating environment variables..."
if check_environment; then
    send_slack_notification "#36a64f" "✅ 환경 설정 완료" "환경 변수가 성공적으로 설정되었습니다." "[
        {\"type\": \"mrkdwn\", \"text\": \"*Status:* Success\"},
        {\"type\": \"mrkdwn\", \"text\": \"*Location:* ${APP_DIR}/.env\"}
    ]"
    log "Environment setup completed successfully"
    exit 0
else
    log "Environment setup failed"
    send_slack_notification "#dc3545" "❌ 환경 설정 실패" "환경 변수 설정에 실패했습니다." "[
        {\"type\": \"mrkdwn\", \"text\": \"*Status:* Failed\"},
        {\"type\": \"mrkdwn\", \"text\": \"*Location:* ${APP_DIR}/.env\"}
    ]"
    exit 1
fi