cd /home/ubuntu/app

# 현재 실행 중인 컨테이너 확인
CURRENT_CONTAINER=$(docker ps --filter "status=running" --filter "name=spring-boot-blue" -q)

if [ -n "$CURRENT_CONTAINER" ]; then
    NEW_CONTAINER="spring-boot-green"
    OLD_CONTAINER="spring-boot-blue"
else
    NEW_CONTAINER="spring-boot-blue"
    OLD_CONTAINER="spring-boot-green"
fi

# nginx 설정 업데이트
sed -i "s/server spring-boot-[^:]*:8080/server $NEW_CONTAINER:8080/g" nginx.conf
docker-compose restart nginx

# 이전 컨테이너 종료
docker stop $OLD_CONTAINER || true
docker rm $OLD_CONTAINER || true