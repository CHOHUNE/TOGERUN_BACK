#!/bin/bash

APP_DIR="/home/ubuntu/app"
LOG_FILE="${APP_DIR}/logs/deployment.log"

BLUE_PORT=8081
GREEN_PORT=8082

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
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


# 컨테이너 헬스체크 (공통 함수로 통합)
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
            return 0
        fi
        attempt=$((attempt + 1))
        sleep $sleep_time
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
    local nginx_container="nginx"

    log "Switching to $target_color deployment..."

    # nginx 컨테이너가 실행 중인지 확인
    if ! docker ps -q -f name=$nginx_container | grep -q .; then
        log "Error: Nginx container is not running"
        return 1
    fi

    # 새로운 설정 적용
    docker exec $nginx_container sh -c "ln -sf /etc/nginx/conf.d/${target_color}.conf /etc/nginx/conf.d/current.conf"

    # nginx 설정 리로드
    if ! docker exec $nginx_container nginx -s reload; then
        log "Error: Failed to reload nginx configuration"
        return 1
    fi

    # switch_nginx 함수에 디버깅 로그 추가
    docker exec $nginx_container ls -l /etc/nginx/conf.d/ || log "Error: Cannot list nginx config directory"
    docker exec $nginx_container cat /etc/nginx/conf.d/current.conf || log "Error: Cannot read current nginx config"

    # nginx 설정 테스트 단계 추가
    if ! docker exec $nginx_container nginx -t; then
        log "Error: Nginx configuration test failed"
        return 1
    fi

    # 헬스체크
    local max_attempts=30
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        log "Health check attempt $attempt of $max_attempts for nginx..."
        if docker exec $nginx_container wget -q --spider http://localhost/health; then
            log "Health check passed for nginx!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 5
    done

    log "Error: Health check failed for nginx"
    return 1
    }

