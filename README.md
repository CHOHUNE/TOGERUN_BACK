

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

