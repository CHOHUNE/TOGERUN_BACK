#!/bin/bash

# 앱 디렉토리 설정
APP_DIR="/home/ubuntu/app"

# 필요한 디렉토리 생성
mkdir -p "$APP_DIR/scripts"
mkdir -p "$APP_DIR/docker"
mkdir -p "$APP_DIR/logs"
mkdir -p "$APP_DIR/certbot/conf"
mkdir -p "$APP_DIR/certbot/www"
mkdir -p "$APP_DIR/ssl"

# 권한 설정
chown -R ubuntu:ubuntu "$APP_DIR"
chmod 755 "$APP_DIR/scripts"
chmod 755 "$APP_DIR/docker"
chmod 755 "$APP_DIR/logs"