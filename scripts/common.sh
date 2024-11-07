#!/bin/bash

APP_DIR="/home/ubuntu/app"
LOG_FILE="${APP_DIR}/logs/deployment.log"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# 컨테이너 헬스체크
check_container_health() {
    local container=$1
    local max_attempts=${2:-30}
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        log "Health check attempt $attempt of $max_attempts for $container..."
        if docker exec $container curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log "Health check passed for $container!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 10
    done
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
        return 1
    fi

    source "$APP_DIR/.env"

    if [ -z "$DOCKER_USERNAME" ]; then
        log "Error: DOCKER_USERNAME is not set"
        return 1
    fi

    if [ -z "$REDIS_PASSWORD" ]; then
        log "Error: REDIS_PASSWORD is not set"
        return 1
    fi
    return 0
}

# Docker 네트워크 확인 및 생성
ensure_network() {
    if ! docker network ls | grep -q "ubuntu_this_network"; then
        log "Creating Docker network: ubuntu_this_network"
        docker network create ubuntu_this_network
    fi
}

# 컨테이너 존재 여부 확인
check_container_exists() {
    local container_name=$1
    docker ps -a --filter "name=$container_name" --format '{{.Names}}' | grep -q "^$container_name$"
}

# 컨테이너 정리 함수
cleanup_container() {
    local container_name=$1
    local timeout=${2:-30}

    if check_container_exists "$container_name"; then
        log "Stopping container $container_name with $timeout seconds timeout..."
        docker stop -t "$timeout" "$container_name" || true
        docker rm "$container_name" || true
    fi
}

# nginx 컨테이너 전환 함수
switch_nginx() {
    local target_color=$1
    local old_color=$([ "$target_color" = "blue" ] && echo "green" || echo "blue")

    log "Switching to $target_color deployment..."

    # 새로운 nginx 시작
    docker-compose -f docker-compose.${target_color}.yml up -d nginx-${target_color} || {
        log "Error: Failed to start nginx-${target_color}"
        return 1
    }

    # 헬스체크
    local max_attempts=30
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        log "Health check attempt $attempt of $max_attempts for nginx-${target_color}..."
        if docker exec nginx-${target_color} wget -q --spider http://localhost/health; then
            log "Health check passed for nginx-${target_color}!"
            break
        fi
        attempt=$((attempt + 1))
        sleep 5
    done

    if [ $attempt -gt $max_attempts ]; then
        log "Error: Health check failed for nginx-${target_color}"
        return 1
    fi

    # 이전 nginx 정리
    if docker ps -q -f name=nginx-${old_color} | grep -q .; then
        log "Stopping old nginx-${old_color}..."
        docker-compose -f docker-compose.${old_color}.yml stop nginx-${old_color}
    fi

    return 0
}

check_container_health_validate() {
    local container=$1
    local port=$2
    local max_attempts=${3:-30}
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        log "Health check attempt $attempt of $max_attempts for $container..."
        if docker exec $container curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
            log "Health check passed for $container!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 10
    done
    return 1
}