#!/bin/bash

APP_DIR="/home/ubuntu/app"
LOG_FILE="${APP_DIR}/logs/deployment.log"

BLUE_PORT=8081
GREEN_PORT=8082

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# Slack 알림 함수
send_slack_notification() {
    local color=$1
    local title=$2
    local message=$3
    local fields=$4

    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        log "Warning: SLACK_WEBHOOK_URL is not set. Skipping notification."
        return 0
    fi

    curl -s -X POST -H 'Content-type: application/json' \
    --data "{
        \"attachments\": [
            {
                \"color\": \"${color}\",
                \"blocks\": [
                    {
                        \"type\": \"section\",
                        \"text\": {
                            \"type\": \"mrkdwn\",
                            \"text\": \"*${title}*\n${message}\"
                        }
                    },
                    {
                        \"type\": \"section\",
                        \"fields\": ${fields}
                    }
                ]
            }
        ]
    }" $SLACK_WEBHOOK_URL

    if [ $? -ne 0 ]; then
        log "Failed to send Slack notification: ${title}"
    fi
}

# deployment target 결정 함수
get_deployment_target() {
    local current_target=$(docker exec nginx readlink /etc/nginx/conf.d/current.conf)

    if [[ $current_target == *"blue"* ]]; then
        echo "green spring-boot-green $GREEN_PORT"
    else
        echo "blue spring-boot-blue $BLUE_PORT"
    fi
}

# cleanup container 함수 추가
cleanup_container() {
    local container=$1
    if docker ps -a | grep -q $container; then
        log "Stopping and removing container $container..."
        docker stop $container >/dev/null 2>&1
        docker rm $container >/dev/null 2>&1
    fi
}

# 컨테이너 헬스체크
check_container_health() {
    local container=$1
    local port=$2
    local max_attempts=${3:-30}
    local attempt=1
    local sleep_time=${4:-10}

    while [ $attempt -le $max_attempts ]; do
        log "Health check attempt $attempt of $max_attempts for $container..."

        if docker exec $container curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
            log "Health check passed for $container!"
            send_slack_notification "#36a64f" "✅ 컨테이너 헬스체크 성공" "컨테이너가 정상적으로 동작 중입니다." "[
                {\"type\": \"mrkdwn\", \"text\": \"*Container:* ${container}\"},
                {\"type\": \"mrkdwn\", \"text\": \"*Port:* ${port}\"}
            ]"
            return 0
        fi

        attempt=$((attempt + 1))
        sleep $sleep_time
    done

    send_slack_notification "#dc3545" "❌ 컨테이너 헬스체크 실패" "컨테이너 상태 확인 실패" "[
        {\"type\": \"mrkdwn\", \"text\": \"*Container:* ${container}\"},
        {\"type\": \"mrkdwn\", \"text\": \"*Attempts:* ${max_attempts}\"}
    ]"
    return 1
}

# 실행 중인 컨테이너 확인
get_running_containers() {
    BLUE_RUNNING=$(docker ps --filter "status=running" --filter "name=spring-boot-blue" -q)
    GREEN_RUNNING=$(docker ps --filter "status=running" --filter "name=spring-boot-green" -q)
}

# 환경 변수 확인
check_environment() {
    if [ ! -f "$APP_DIR/.env" ]; then
        log "Error: .env file not found"
        send_slack_notification "#dc3545" "⚠️ 환경 설정 오류" ".env 파일을 찾을 수 없습니다." "[
            {\"type\": \"mrkdwn\", \"text\": \"*Location:* ${APP_DIR}/.env\"},
            {\"type\": \"mrkdwn\", \"text\": \"*Status:* Missing\"}
        ]"
        return 1
    fi

    source "$APP_DIR/.env"

    local missing_vars=""
    [ -z "$DOCKER_USERNAME" ] && missing_vars+="DOCKER_USERNAME "
    [ -z "$REDIS_PASSWORD" ] && missing_vars+="REDIS_PASSWORD "
    [ -z "$SLACK_WEBHOOK_URL" ] && missing_vars+="SLACK_WEBHOOK_URL "

    if [ ! -z "$missing_vars" ]; then
        log "Error: Required environment variables are not set: $missing_vars"
        send_slack_notification "#dc3545" "⚠️ 환경 변수 오류" "필수 환경 변수가 설정되지 않았습니다." "[
            {\"type\": \"mrkdwn\", \"text\": \"*Missing Variables:* ${missing_vars}\"}
        ]"
        return 1
    fi
    return 0
}

# Docker 네트워크 확인 및 생성
ensure_network() {
    if ! docker network ls | grep -q "ubuntu_this_network"; then
        log "Creating Docker network: ubuntu_this_network"
        docker network create ubuntu_this_network

        if [ $? -eq 0 ]; then
            send_slack_notification "#36a64f" "🌐 네트워크 생성 완료" "Docker 네트워크가 생성되었습니다." "[
                {\"type\": \"mrkdwn\", \"text\": \"*Network:* ubuntu_this_network\"},
                {\"type\": \"mrkdwn\", \"text\": \"*Status:* Created\"}
            ]"
        else
            send_slack_notification "#dc3545" "❌ 네트워크 생성 실패" "Docker 네트워크 생성에 실패했습니다." "[
                {\"type\": \"mrkdwn\", \"text\": \"*Network:* ubuntu_this_network\"},
                {\"type\": \"mrkdwn\", \"text\": \"*Status:* Failed\"}
            ]"
        fi
    fi
}

# nginx 컨테이너 전환 함수
switch_nginx() {
    local target_color=$1
    local nginx_container="nginx"

    log "Switching to $target_color deployment..."
    send_slack_notification "#36a64f" "🔄 트래픽 전환 시작" "Nginx 설정을 변경합니다." "[
        {\"type\": \"mrkdwn\", \"text\": \"*Target:* ${target_color}\"},
        {\"type\": \"mrkdwn\", \"text\": \"*Container:* ${nginx_container}\"}
    ]"

    if ! docker ps -q -f name=$nginx_container | grep -q .; then
        send_slack_notification "#dc3545" "❌ Nginx 오류" "Nginx 컨테이너가 실행중이지 않습니다." "[
            {\"type\": \"mrkdwn\", \"text\": \"*Container:* ${nginx_container}\"},
            {\"type\": \"mrkdwn\", \"text\": \"*Status:* Not Running\"}
        ]"
        return 1
    fi

    docker exec $nginx_container sh -c "ln -sf /etc/nginx/conf.d/${target_color}.conf /etc/nginx/conf.d/current.conf"

    if ! docker exec $nginx_container nginx -t; then
        send_slack_notification "#dc3545" "❌ Nginx 설정 오류" "Nginx 설정 테스트에 실패했습니다." "[
            {\"type\": \"mrkdwn\", \"text\": \"*Config:* ${target_color}.conf\"},
            {\"type\": \"mrkdwn\", \"text\": \"*Status:* Test Failed\"}
        ]"
        return 1
    fi

    if ! docker exec $nginx_container nginx -s reload; then
        send_slack_notification "#dc3545" "❌ Nginx 재시작 실패" "Nginx 설정 리로드에 실패했습니다." "[
            {\"type\": \"mrkdwn\", \"text\": \"*Action:* Reload\"},
            {\"type\": \"mrkdwn\", \"text\": \"*Status:* Failed\"}
        ]"
        return 1
    fi

    send_slack_notification "#36a64f" "✅ 트래픽 전환 완료" "Nginx 설정이 성공적으로 변경되었습니다." "[
        {\"type\": \"mrkdwn\", \"text\": \"*Active:* ${target_color}\"},
        {\"type\": \"mrkdwn\", \"text\": \"*Status:* Success\"}
    ]"
    return 0
}