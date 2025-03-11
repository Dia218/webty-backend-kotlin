# webty-backend-kotlin

> 프로그래머스 데브코스: 팀 프로젝트 #3 코틀린

한국 웹툰 커뮤니티 'WEBTY'  
백엔드


<br>

# WEBTY

10대를 위한 웹툰 커뮤니티 플랫폼  
사용자들은 웹툰 리뷰를 공유하고, 유사한 웹툰에 대해 토론할 수 있습니다


<br>

## 기술 스택

- **언어 및 프레임워크:** Java 21, Spring Boot 3
- **인증:** Spring Security, JWT, OAuth2 (Kakao)
- **데이터베이스:** MySQL, H2 (테스트)
- **캐싱 / 세션 관리:** Redis
- **메세지 브로커:** Redis
- **실시간 처리:** STOMP WebSocket
- **사용자 정보 분석:** Elastic Search
- **부하테스트 및 시각화:** k6 + Grafana + InfluxDB
- **환경 구축:** docker compose
- **API 문서화:** Swagger

<br>

## 도메인

common: 각 도메인에서 공통으로 사용하는 부분
security: 인증 관리
user: 계정 관리
userActivity: 사용자 맞춤 웹툰 추천
webtoon: 웹툰 크롤링
review: 리뷰 게시글
reviewComment: 댓글
recommend: 게시글 추천
search: 검색
voting: 웹툰 유사도 투표

<br>

## ERD

[ERDCloud](https://www.erdcloud.com/d/W5oyCz7sXaAzScd4X)

<br>

## 환경 구축

### 개발 환경 구축

```
docker-compose -f docker/webty-dev/docker-compose.dev.yml up --build
```

데이터베이스 초기화 시 docker volume 삭제 필요:

```
//volume 목록 확인
docker volume ls

//volume 삭제
docker volume rm volume_name
```

### k6 테스트 환경 구축

```
docker-compose -f docker/webty-k6/docker-compose.k6.yml up --build
```

실행 하는 방법:

```
// 토큰이 필요하지 않은 경우
docker exec webty-k6 k6 run /scripts/voting/similar-test.js

// 토큰이 필요한 경우
docker exec -e K6_TOKEN="토큰을넣어주세요" webty-k6 k6 run /scripts/voting/similar-test.js
```

<br>

## Swagger

swagger: [서버 실행 후 접속](http://localhost:8080/swagger-ui/index.html)


<br>

## 디렉터리 구조

```
D:.
├─docker   # Dockerfile, docker-compose.yml
│  ├─webty-dev
│  └─webty-k6
├─gradle
│  └─wrapper
└─src
    ├─main
    │  ├─generated
    │  ├─kotlin
    │  │  └─org
    │  │      └─team14
    │  │          └─webty
    │  │              ├─common
    │  │              │  ├─config
    │  │              │  ├─cookies
    │  │              │  ├─dto
    │  │              │  ├─entity
    │  │              │  ├─enums
    │  │              │  ├─exception
    │  │              │  ├─mapper
    │  │              │  ├─redis
    │  │              │  └─util
    │  │              ├─recommend
    │  │              │  ├─controller
    │  │              │  ├─entity
    │  │              │  ├─enums
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─review
    │  │              │  ├─cache
    │  │              │  ├─controller
    │  │              │  ├─dto
    │  │              │  ├─entity
    │  │              │  ├─enums
    │  │              │  ├─mapper
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─reviewComment
    │  │              │  ├─controller
    │  │              │  ├─dto
    │  │              │  ├─entity
    │  │              │  ├─mapper
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─search
    │  │              │  ├─constants
    │  │              │  ├─controller
    │  │              │  ├─dto
    │  │              │  ├─enums
    │  │              │  ├─mapper
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─security
    │  │              │  ├─authentication
    │  │              │  ├─config
    │  │              │  ├─oauth2
    │  │              │  ├─policy
    │  │              │  └─token
    │  │              ├─user
    │  │              │  ├─controller
    │  │              │  ├─dto
    │  │              │  ├─entity
    │  │              │  ├─enums
    │  │              │  ├─mapper
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─userActivity
    │  │              │  ├─controller
    │  │              │  ├─document
    │  │              │  ├─dto
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              ├─voting
    │  │              │  ├─cache
    │  │              │  ├─controller
    │  │              │  ├─dto
    │  │              │  ├─entity
    │  │              │  ├─enums
    │  │              │  ├─listener
    │  │              │  ├─mapper
    │  │              │  ├─message
    │  │              │  ├─repository
    │  │              │  └─service
    │  │              └─webtoon
    │  │                  ├─api
    │  │                  ├─controller
    │  │                  ├─dto
    │  │                  ├─entity
    │  │                  ├─enums
    │  │                  ├─infrastructure
    │  │                  ├─mapper
    │  │                  ├─repository
    │  │                  ├─schedueler
    │  │                  └─service
    │  └─resources   # 애플리케이션 환경 설정 파일
    │      └─docs
    └─test
        ├─k6   # k6 부하 테스트
        └─kotlin   # 단위 테스트
```

