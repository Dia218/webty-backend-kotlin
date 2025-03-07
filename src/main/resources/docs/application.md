```
api:
  title: Webty
  description: 좋아하는 웹툰을 이야기해보세요!
  version: 1.0.0

server:
  url: http://localhost:8080/

spring:
  profiles:
    active: prod
  application:
    name: webty
  data:
    redis:
      host: localhost
      port: 6379
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
  elasticsearch:
    uris: http://localhost:9200

  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: ${OAUTH_KAKAO_CLIENT_ID}
            client-secret: ${OAUTH_KAKAO_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: ${server.url}${app.oauth2.redirect-uri-suffix}
            client-name: Kakao
            client-authentication-method: client_secret_post

        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

app:
  oauth2:
    redirect-uri-suffix: ${OAUTH_KAKAO_REDIRECT_URI_SUFFIX}

jwt:
  secret: V4r8jL3wqCz7oP7Y7Jl2kJcB9QxZlH4sRmGgqT9PZ8fF0e1yP1zDgT3zK7Qw3Fw
  redirect: http://localhost:3000/callback
  access-token:
    expiration-time: 3600000
  refresh-token:
    expiration-time: 604800000
    
upload:
  path: ${UPLOAD_PATH:../webty-frontend-next/public/uploads/}

default-profile-image: ${DEFAULT_PROFILE_IMAGE:/uploads/iconmonstr-user-circle-thin-240.png}

```