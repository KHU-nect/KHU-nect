# Khunect Backend Agent Guide

## 프로젝트 개요
- 서비스명은 쿠넥트(Khunect)이며 경희대 학생 대상 매칭/수업/채팅 플랫폼 백엔드다.
- 기술 스택은 Spring Boot, Gradle Groovy, Java 21, JPA, REST API, WebSocket(STOMP)다.
- 패키지 루트는 `com.khunect.backend`를 유지한다.
- 프론트엔드와 분리된 구조이므로 인증 성공 후 브라우저에 토큰을 직접 노출하지 않는다.

## 코딩 규칙
- Controller에는 요청 파싱과 응답 반환만 두고 비즈니스 로직은 Service로 분리한다.
- Entity, Request DTO, Response DTO를 분리한다.
- 범위를 벗어난 대규모 리팩터링은 하지 않는다.
- 공통 기능은 `common` 패키지에 두고 도메인 기능은 도메인 패키지별로 응집시킨다.
- Validation 애너테이션을 적극적으로 사용하고 수동 검증은 최소화한다.
- 시간 컬럼은 감사 기반 공통 엔티티를 통해 관리한다.

## 도메인 규칙
- 인증은 Google OAuth2 기반이며 `@khu.ac.kr` 이메일만 허용한다.
- 최초 로그인 이후 프로필 설정 단계를 분리한다.
- 수업, 시간표, 채팅, 매칭, 마이페이지, 관심사, 크롤러는 도메인별 패키지로 나눈다.
- 채팅은 수업 채팅방과 매칭 성사 후 1:1 채팅을 분리해 설계한다.
- 지금 단계에서는 공통 기반과 설정만 만들고 도메인 기능 구현은 다음 단계로 미룬다.

## 테스트/검증 규칙
- 변경 후 Windows 기준으로 `.\gradlew.bat test`와 `.\gradlew.bat bootJar`를 실행한다.
- 테스트가 깨지면 원인을 확인하고 같은 작업 범위 안에서 함께 수정한다.
- 프로필별 설정 차이로 테스트가 흔들리지 않도록 `test` 프로필을 분리한다.

## 응답/예외 규칙
- 모든 API 응답은 `ApiResponse<T>` 형식을 기본으로 사용한다.
- 예외 응답은 `ErrorResponse`로 통일하고 `GlobalExceptionHandler`에서 처리한다.
- 비즈니스 예외는 `CustomException`과 `ErrorCode` 조합으로 던진다.
- Validation 실패, 타입 불일치, 인증/인가 예외도 공통 포맷으로 변환한다.

## WebSocket/JWT/OAuth 관련 규칙
- WebSocket은 STOMP 기반으로 구현하고 JWT 인증을 적용한다.
- HTTP API 인증도 JWT 기반으로 처리한다.
- Google OAuth2 로그인 성공 시 프론트 콜백으로는 1회용 auth code만 전달한다.
- 프론트는 별도 `/api/auth/exchange` API로 access token, refresh token을 교환한다.
- JWT secret, OAuth client 정보, 프론트 URL은 환경 변수로 주입하고 설정 파일에는 placeholder만 둔다.
