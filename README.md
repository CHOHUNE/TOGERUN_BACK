

# Togerun


![](https://velog.velcdn.com/images/yuureru/post/350fdf86-939d-4ed1-b4de-c84529c1c2b5/image.png)

# 1. 프로젝트 개요

- **Tomato** : **[게시판 기반 채팅 웹 앱 서비스]**
- **프로젝트 기간** : 2024.06.11~
- **프로젝트 인원** : 1명.
- **개발언어** : JAVA 17, React.
- **개발 환경** :
	- **백엔드:** SpringBoot, SpringSecurity, Oauth2.0, JPA, Redis, nginX, gradle.
	- **프론트엔드**: react, tail-wind, react-query,recoil.
- **데이터 베이스**: MariaDB.
- **관리 툴**: github.
- **배포:** githubactions, Docker, EC2, RDS, S3, Vercel.
- **간단 소개**: 게시판 기반의 운동 모임 매칭 웹 앱 서비스.


![](https://velog.velcdn.com/images/yuureru/post/65bf45d6-2ecc-436a-98c0-148ec366be45/image.png)

### 1-2아키텍처
![](https://velog.velcdn.com/images/yuureru/post/d7f655f2-7d51-4d3b-a9c9-355a41a00576/image.png)

### 1-3 화면 흐름
![](https://velog.velcdn.com/images/yuureru/post/18792759-45fa-4935-8ff7-dc6ef02bca0a/image.png)


# 2. 기능 설계

- 2.1. **유저 기능**
    - **소셜 로그인** : Google, Kakao, Naver 를 이용해 로그인.
    - **마이페이지,정보 수정** : 닉네임, 성별, 연령대, 연락처 수정. 
- 2.2. **게시판 기능**
    - **글 작성**
        - 제목, 내용, 집결 시간, 활동 유형, 모집 정원, 집결 장소, 이미지를 첨부하여 작성.
        - 카카오맵 API 를 이용하여 집결 장소 태그.
    - **글 조회**
        - 제목, 내용, 이미지, 작성자, 작성일, 좋아요, 댓글, 활동 유형, 마감 유무, 주소확인.
        - 로그인한 유저만 댓글 추가 가능.
    - **글 수정**
        - 제목, 내용, 이미지 수정 가능.
        - 해당 글의 작성자, 관리자만 삭제 가능.
    - **글 삭제**
        - 작성자, 관리자만 삭제 가능.
        - 해당 글의 댓글, 좋아요 모두 삭제.
    - **댓글 기능**
        - 로그인한 유저만 댓글 작성 가능.
        - 작성자와 관리자, 해당 댓글 작성자만 댓글 수정, 삭제 가능.
        - 무한 대댓글.
    - **즐겨찾기**
    	- CRUD.
    	- 즐겨찾기 탭에서 별도로 확인 가능.
   	- **좋아요**
    	- CURD.
        
- 2.3.**등급기능**
    - BRONZE, SILVER, ADMIN, SYSTEM
        - 최초 가입시 BRONZE 등급 설정
            - 서비스 이용 불가.
            - 메뉴 클릭시 회원 정보 수정 페이지로 redriect 처리 
        - 추가 정보 기입시 SIVLER 등급 상승
            - 서비스 이용 가능.
        - 관리자 계정
        	- 모든 글, 댓글 수정 삭제.
            - 회원 탈퇴 처리.
- 2.4 **채팅 기능**
    - 로그인한 유저만 채팅 가능.
    - 게시물에서 설정한 정원만큼 입장 가능.
    - 사이드바에서 참여한 채팅방 일괄 확인.
    - WebStomp와 Redis 를 활용한 실시간 메시지 전송 가능.
- 2.5 **알림 기능**
    - SSE를 활용한 실시간 알림 기능.
    - 채팅 메세지, 댓글, 좋아요 알람 기능.
- 2.6 **관리자 기능**
    - 유저삭제, 댓글, 글 삭제

## API 기능 명세

| 엔드포인트                                    | 메소드     | 설명            | 요청 파라미터                                                         | 응답                                               | 필요 권한  |
| ---------------------------------------- | ------- | ------------- | --------------------------------------------------------------- | ------------------------------------------------ | ------ |
| /api/member/refresh                      | POST    | 토큰 갱신         | Header: Authorization                                           | Map\<String, Object> (accessToken, refreshToken) | 없음     |
| /api/user/check/{nickname}               | GET     | 닉네임 중복 확인     | PathVariable: nickname(String)                                  | Map\<String, Boolean> (available)                | 없음     |
| /api/user/info                           | GET     | 사용자 정보 조회     | -                                                               | UserDTO                                          | 인증     |
| /api/user/modify                         | PUT     | 사용자 정보 수정     | UserModifyDTO                                                   | Map\<String, Object>                             | 인증     |
| /api/user/joined                         | GET     | 참여 채팅방 조회     | -                                                               | List\<UserChatRoomDTO>                           | 인증     |
| /api/user/favorites                      | GET     | 즐겨찾기 목록 조회    | -                                                               | List\<FavoriteDTO>                               | 인증     |
| /api/notifications/subscribe             | GET     | SSE 알림 구독     | Header: Last-Event-ID                                           | SseEmitter                                       | 인증     |
| /api/notifications/all                   | GET     | 전체 알림 조회      | RequestParam: page(int), size(int)                              | NotifyDto.PageResponse                           | 인증     |
| /api/notifications/{notificationId}/read | POST    | 알림 읽음 처리      | PathVariable: notificationId(Long)                              | void                                             | 인증     |
| /api/notifications/unread/count          | GET     | 읽지 않은 알림 수 조회 | -                                                               | Long                                             | 인증     |
| /api/notifications/clear                 | POST    | 전체 알림 읽음 처리   | -                                                               | void                                             | 인증     |
| /api/comment                             | POST    | 댓글 생성         | CommentRequestDto                                               | CommentResponseDto                               | 인증     |
| /api/comment                             | PUT     | 댓글 수정         | CommentRequestDto                                               | CommentResponseDto                               | 인증     |
| /api/comment/{postId}                    | GET     | 게시물의 댓글 조회    | PathVariable: postId(Long)                                      | List\<CommentResponseDto>                        | 인증     |
| /api/comment/{commentId}                 | DELETE  | 댓글 삭제         | PathVariable: commentId(Long)                                   | Long                                             | 인증     |
| /chat/{postId}/send                      | MESSAGE | 채팅 메시지 전송     | DestinationVariable: postId(Long), Payload: ChatMessageDTO      | ChatMessageDTO                                   | 인증     |
| /api/post/{postId}/chat/join             | POST    | 채팅방 입장        | PathVariable: postId(Long)                                      | ChatRoomDTO                                      | 인증     |
| /api/post/{postId}/chat/leave            | POST    | 채팅방 퇴장        | PathVariable: postId(Long)                                      | String(성공 메시지)                                   | 인증     |
| /api/post/{postId}/chat                  | GET     | 채팅 메시지 조회     | PathVariable: postId(Long)                                      | List\<ChatMessageDTO>                            | 인증     |
| /api/post/{postId}/chat/status           | GET     | 채팅방 상태 조회     | PathVariable: postId(Long)                                      | ChatRoomDTO                                      | 인증     |
| /api/post                                | GET     | 전체 게시물 조회     | -                                                               | List\<Post>                                      | SILVER |
| /api/post                                | POST    | 게시물 생성        | MultipartFile: uploadFiles(선택), PostDTO                         | Map\<String, Long> (id)                          | SILVER |
| /api/post/{id}                           | GET     | 특정 게시물 조회     | PathVariable: id(Long)                                          | Post                                             | SILVER |
| /api/post/list                           | GET     | 게시물 페이징 조회    | PageRequestDTO                                                  | PageResponseDTO\<PostListDTO>                    | SILVER |
| /api/post/{id}                           | PUT     | 게시물 수정        | PathVariable: id(Long), MultipartFile: uploadFiles(선택), PostDTO | Map\<String, String>                             | SILVER |
| /api/post/{id}                           | DELETE  | 게시물 삭제        | PathVariable: id(Long)                                          | Map\<String, Object>                             | SILVER |
| /api/post/{id}/favorite                  | POST    | 게시물 즐겨찾기 토글   | PathVariable: id(Long)                                          | FavoriteDTO                                      | SILVER |
| /api/post/{id}/like                      | POST    | 게시물 좋아요 토글    | PathVariable: id(Long)                                          | LikeDTO                                          | SILVER |
| /api/admin/users                         | GET     | 전체 사용자 조회     | -                                                               | List\<UserDTO>                                   | ADMIN  |
| /api/admin/users/{userId}/delete         | PUT     | 사용자 소프트 삭제    | PathVariable: userId(Long)                                      | void                                             | ADMIN  |
| /api/admin/users/{userId}/restore        | PUT     | 삭제된 사용자 복구    | PathVariable: userId(Long)                                      | UserDTO                                          | ADMIN  |


## DB설계

![](https://velog.velcdn.com/images/yuureru/post/9bbd6dee-4867-4603-9423-88f788983fd4/image.png)

### user 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 사용자 식별자 |
| is_deleted | bit(1) | | 삭제 여부 |
| social | bit(1) | | 소셜 로그인 여부 |
| deleted_at | datetime(6) | | 삭제 일시 |
| age | varchar(255) | | 나이 |
| email | varchar(255) | | 이메일 |
| gender | varchar(255) | | 성별 |
| img | varchar(255) | | 프로필 이미지 |
| mobile | varchar(255) | | 휴대폰 번호 |
| name | varchar(255) | | 이름 |
| nickname | varchar(255) | | 닉네임 |
| password | varchar(255) | | 비밀번호 |

### post 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 게시글 식별자 |
| capacity | int(11) | | 참여 가능 인원 |
| del_flag | bit(1) | | 삭제 플래그 |
| latitude | double | | 위도 |
| longitude | double | | 경도 |
| local_date | date | | 로컬 날짜 |
| participate_flag | tinyint(1) | | 참여 플래그 |
| chat_room_id | bigint(20) | FK | 채팅방 ID |
| meeting_time | datetime(6) | | 미팅 시간 |
| user_id | bigint(20) | FK | 작성자 ID |
| view_count | bigint(20) | | 조회수 |
| title | varchar(100) | | 제목 |
| content | varchar(255) | | 내용 |
| place_name | varchar(255) | | 장소명 |
| road_name | varchar(255) | | 도로명 |
| activity_type | enum | | 활동 유형(climbing, cycling, hiking, pilates, running, surfing, weight_training, yoga) |

### chat_rooms 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 채팅방 식별자 |
| activity_type | tinyint(4) | | 활동 유형 |
| can_join | bit(1) | | 참여 가능 여부 |
| participant | bit(1) | | 참여자 여부 |
| participant_count | int(11) | | 참여자 수 |

### chat_messages 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 메시지 식별자 |
| chat_room_id | bigint(20) | FK | 채팅방 ID |
| created_at | datetime(6) | | 생성 일시 |
| user_id | bigint(20) | FK | 작성자 ID |
| content | varchar(500) | | 메시지 내용 |
| chat_message_type | enum | | 메시지 타입(normal, system) |

### comment 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| comment_id | bigint(20) | PK | 댓글 식별자 |
| del_flag | bit(1) | | 삭제 플래그 |
| created_at | datetime(6) | | 생성 일시 |
| parent_id | bigint(20) | FK | 부모 댓글 ID |
| post_id | bigint(20) | FK | 게시글 ID |
| created_by | varchar(255) | | 작성자 |
| img | varchar(255) | | 이미지 |
| nick_name | varchar(255) | | 닉네임 |
| content | tinytext | | 댓글 내용 |

### notify 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| notification_id | bigint(20) | PK | 알림 식별자 |
| is_read | bit(1) | | 읽음 여부 |
| created_at | datetime(6) | | 생성 일시 |
| post_id | bigint(20) | FK | 게시글 ID |
| user_id | bigint(20) | FK | 사용자 ID |
| content | varchar(255) | | 알림 내용 |
| url | varchar(255) | | 알림 URL |
| notification_type | enum | | 알림 타입(chat, comment, like, system) |

### post_like 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 좋아요 식별자 |
| is_active | bit(1) | | 활성화 여부 |
| created_at | datetime(6) | | 생성 일시 |
| post_id | bigint(20) | FK | 게시글 ID |
| user_id | bigint(20) | FK | 사용자 ID |

### post_favorite 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| id | bigint(20) | PK | 즐겨찾기 식별자 |
| is_active | bit(1) | | 활성화 여부 |
| created_at | datetime(6) | | 생성 일시 |
| post_id | bigint(20) | FK | 게시글 ID |
| user_id | bigint(20) | FK | 사용자 ID |

### post_images 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| post_id | bigint(20) | PK, FK | 게시글 ID |
| file_name | varchar(255) | | 파일명 |
| ord | int(11) | | 정렬 순서 |

### user_user_role_list 테이블
| 컬럼명 | 데이터타입 | 조건 | 설명 |
|--------|------------|------|------|
| user_id | bigint(20) | PK, FK | 사용자 ID |
| user_role_list | enum | | 사용자 권한(role_admin, role_bronze, role_silver, role_system) |

### chat_room_participants 테이블
| 컬럼명          | 데이터타입      | 조건     | 설명     |
| ------------ | ---------- | ------ | ------ |
| chat_room_id | bigint(20) | PK, FK | 채팅방 ID |
| user_id      | bigint(20) | PK, FK | 사용자 ID |


## 5. 기능 구현

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/303a043c-71e3-4d61-b357-dcef160a4f2e/image.png)

**소셜 로그인**

- SpringSecurity Oauth2.0 JWT 인증 방식 사용
- RefreshToken은 Redis 저장
- AccessToken은 Cookie 저장
- 각 소셜로그인에서 제공하는 정보를 가져 옵니다.
- ( 이미지, 이름, 닉네임, 이메일 )
- 회원 정보 값에 공란이 있을 경우 다른 기능 이용시 회원 정보 수정으로 Redirect 처리
- 중복되는 이메일 방지를 위하여 이메일 뒤에 언더바 뒤에 출처 추가

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/eb4679ca-b09d-4437-98bc-31d9fb23b7db/image.png)

**게시판**

- PC, 태블릿, 모바일 반응형 디자인 적용
- Querydsl 동적 쿼리 생성을 이용한 지역, 종목 선택 및 검색어 입력
- JPA Pagenation 적용
- 타이틀, 닉네임, 작성 날짜, 참여 가능 여부, 조회수, 좋아요 카운트, 활동 타입, 집결 장소 도로명 주소 기재

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/52c88586-1519-4e4b-8b58-c848650e9cb8/image.png)

**게시판**

- 글 상세 반응형 디자인 적용
- 좋아요, 즐겨찾기, 채팅방 입장, 댓글 기능
- 카카오 맵 API 를 이용한 위치 태그 표시
- 글 작성자, ADMIN 계정 수정 삭제 가능
- 무한 대댓글 기능
- 삭제시 대댓글이 없는 경우 바로 소거
- 삭제시 대댓글이 있는 경우 삭제된 댓글로 표시
- 댓글 당사자 수정, 삭제 가능.
- ADMIN 계정 모든 댓글 삭제만 가능. ( 수정 불가 )

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/e12b6d5f-7c60-49b0-a000-84d1d50fcfa3/image.png)

**즐겨찾기**

- 즐겨찾기 모아보기 기능
- 상세 클릭시 해당 게시글로 이동
- 삭제시 가능

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/78899158-8fe6-4bb3-b85b-d60f5b72633b/image.png)

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/6e37e26d-2264-4d65-86c5-45f37aaead5a/image.png)

**채팅방**

- 채팅방 입장시 게시글에 해당하는 채팅방 생성
- Stomp, Redis 를 활용해 구현
- Stomp : WebSocket 연결 관리,라우팅 규칙 제공
- Redis : Pub, Sub, 인메모리를 이용한 빠른 메세지 처리
- 해당 채팅방 페이지 벗어날시 웹소켓 연결 종료
- 상단 네비바에서 채팅방 목록 확인 기능
- 채팅방 나가기 버튼 클릭시 목록에서 삭제

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/8fcf861f-65ee-40a4-86a2-36b52de6cac9/image.png)

**SSE 알람**

- 사이트 접속시 SSE 항시 연결
- 댓글, 채팅방, 좋아요 실시간 알람
- 더 보기 버튼 useInfiniteQuery 적용
- 모두읽기 버튼으로 알람 일괄 읽기 처리
- AOP pointcut 을 애너테이션으로 처리
- 해당 알람 클릭시 게시물, 혹은 채팅방으로 이동

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/222be66c-8b34-43dd-b611-d428b8782b80/image.png)

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/a901380f-336f-4bb4-a1e9-a11c86acb42c/image.png)

**SSE 알람**

- AOP 포인트 컷에 애너테이션 적용
- 댓글 작성, 메세지 보내기, 라이크 메서드에 애너테이션 적용

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/d9ac26ac-12f8-4fef-9fae-f0b6a2c09658/image.png)

**카카오 맵 장소 태깅**

- 글 작성 페이지 내 장소 검색 후 선택 가능
- 글 상세 페이지 해당 장소 확인 가능
- 글 상세 페이지 내 장소명과 도로명 주소 확인 가능
- 게시판 페이지 해당 글 도로명 주소 확인 및 검색 가능

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/dae0ff1f-9734-4310-a457-65102ba2f8ef/image.png)

**Redis 조회수 중복 방지**

- 글 조회시 view:해당게시물ID:IP에 해당하는 값 Redis 에 저장
- 해당 값은 1일 후에 자동 소멸
- 해당 값이 없을 때에만 Redis 인메모리에 저장
- 해당 값이 없을시 false 반환
- False 일시에 조회수 증가

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/a5532989-c9c4-42d0-b6f2-df885cb310b7/image.png)

**Redis 캐싱,RefreshToken 저장**

- 게시물과 코멘트 캐싱 처리
- Cache-Aside 패턴 구현
- 수정 혹은 삭제시 캐시 삭제
- 빈번히 조회되고 상대적으로 수정, 삭제가 덜 빈번하여 해당 패턴 적용
- 보안 강화를 위해 RefreshToken 저장, 12시간 후 만료

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/cb8b842b-bc31-435a-a5af-30baaa91f938/image.png)

**Nginx** **Reverse Proxy**

- Blue green 무중단 배포 환경 구축
- 동적 업스트림 전환
- CodeDeploy 단계에서 동적으로 blue/ green 중 해당하는 upstream conf 파일로 심볼릭 링크 변경
- 443 포트로 들어온 트래픽을 8081 혹은 8082로 전환

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/7cff6025-8078-43ac-aa45-e0cbee33ba01/9b8a440e-94fc-477a-ba73-dc80ac4a4e9a/image.png)

**Nginx** **보안 관련**

- 인프라 단계에서 CORS 정책 해결과 XSS 헤더 추가
- Let’s Encrypt 인증서를 사용해 SSL 레이어 구성
- Cerbot을 이용해 12시간 마다 갱신 확인
- 가비아에서 도메인 구매 후

**Github Actions를 통한 CI**

- 빌드 환경 구성 : Gradle, JDK17, Ubuntu
- 애플리케이션 빌드 : 환경변수 주입, JAR 파일 생성
- Docker 이미지 생성: 이미지 빌드, Docker hub push
- 환경변수는 githubactions repository secerts 에서 관리
- docker hub 에 업로드 한 container 는 private 처리

**CodeDeploy를 통한 CD**

- 배포 패키지 구성 :nginx.conf, docker-compose.yml
- 배포스크립트 구성
- SSL 인증서 설정
- S3 버킷 배포 패키지 업로드
- CodeDeploy 를 통한 EC2 배포 실행
- 배포 log 기록

**무중단 배포 구현**

- Blue (8081), Green (8082) 환경 구성
- Health Check
- Nginx 리버스 프록시, 로드밸런싱 설정
- 컨테이너 : Spring, Redis, Nginx, Certbot (SSL)

**Keep**

배포, 백엔드, 프론트 실제 전체적인 개발 사이클 경험
CICD 파이프라인 구축과 무중단 배포 환경 구성
개발 외 프로젝트 규모와 비용적 측면을 고려한 기술 스택 의사 결정 경험
실시간 통신이 필요한 기능들에 대한 적절한 기술 적용 ( 채팅 : 웹소켓, 알람 : SSE )
보안을 고려한 인증/ 인가 시스템 설계, SSL 계층 적용, RefreshToken Redis 별도저장
인가되지 않은 사용자 요청에 대한 리다이렉트 처리

**Problem**

체계 적인 문서화 부족
트러블 슈팅 과정에 대한 해결책과 기록 다소 미흡
개발 프로세스 관리
리소스 낭비가 우려되는 오버엔지니어링
명확한 설계 없이 진행하여 중복 컴포넌트 발생으로 개발 일정 지체
1인 프로젝트 진행으로 체계적인 일정 관리와 우선순위 미흡
재사용 가능한 컴포넌트 설계 다소 미흡

**기술적 의사 결정**

1. 일반 회원가입 없이 소셜 로그인만 구현한 이유 
    1. Oauth2.0 프로토콜 활용으로 보안 강화
    2. 사용자의 민감한 정보 직접 관리 리스크 제거
    3. 즉시 SNS 계정으로 서비스 이용으로 사용자 경험 개선
2. 토큰 기반 인증 아키텍처
    1. accessToken 
        1. stateless 서버 부하 감소
        2. 짧은 유효기간으로 탈취 위험 감소
    2. refreshToken Redis 저장시 이점
        1. in-memory 처리로 인증 성능 향상
        2. Key-Value 구조로 토큰 관리 용이
        3. TTL 기능으로 만료 처리 자동화
3. 실시간 통신 프로토콜 선택 (WebSocket & ServerSentEvent )
    1. Websocet (채팅 적용)
        1. Stomp 프로토콜로 메시지 포맷 표준화
        2. 양방향 통신으로 실시가 대화 구현
    2. SSE (알림)
        1. 단방향 통신으로 항시 연결을 유지하는데 Websocket 보다 리소스 관리가 효율적
4. CORS 정책 인프라 레벨 구현
    1. SpringSecurity 에 구현하지 않고 바로 Nginx 에서 CORS 처리
        1. 서버 부하 감소
        2. CORS SSL 등 통합 관리
5. 채팅 시스템 아키텍처
    1. Stomp + Redis
        1. Stomp
            1. 채팅방 구독 / 해지 기능 구현 
            2. 메세지 타입 및 라우팅 표준화
        2. Redis
            1. Pub/Sub 실시간 메세지 전송
            2. 채팅방 정보 캐싱
6. 상태관리 라이브러리
    1. Redux vs Recoil
        1. 보일러 플레이트 코드 감소
        2. 비교적 소규모 프로젝트로 간단한 상태관리만 필요로 했음.
7. CI/CD 파이프라인 
    1. jenkins 대신 github actions을 선택한 이유 
        1. gitHub 통합 환경으로 비교적 간단한 구현
        2. 소규모 프로젝트로 복잡한 커스텀이 필요 없음.
        3. 별도의 CI 서버가 필요 없음
    2. AWS CodeDeploy 선택한 이유 
        1. blue-green 배포 구현
        2. 자동 롤백 기능
8. SSL 인증서 관리
    1. CloudFront 와 route53 대신 도메인 직접 구매,Let’s Encrypt+Cerbot+nginx 
        1. 비용적인 측면 무료 인증서 발급과 자동 갱신 스크립트 
9. 웹 서버 아키텍처
    1. Nginx 
        1. 로드밸런싱 ( blue, green 배포)
        2. SSL 관리
        3. 정적 컨텐츠 캐싱
10. 무중단 배포 Blue Green 전략
    1. 롤백시 관리 용이
    2. 간단한 구현 
    3. 헬스 체크 통합
11. 성능 최적화 
    1. Redis : 빈번한 데이터 조회로 Cache-Aside 전략 선택 
12. ORM Mybatis → JPA
    1. 반복적 CURD 코드 제거로 개발 생산성 증대
    2. 상속 구조 표현 
13. QueryDSL 
    1. 타입 safe 해서 컴파일 시점시 문법 검증. 실패시 build 되지 않음
    2. 동적 조건 처리와 서브쿼리 지원 
14. UI 프레임 워크 ChakuraUI → TailWindCSS
    1. 반응형 디자인 용이
