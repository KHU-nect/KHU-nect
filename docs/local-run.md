# Local Run

## 환경
- Java 21
- Windows 기준 명령:
  - `.\gradlew.bat test`
  - `.\gradlew.bat bootRun`

## 기본 프로필
- 기본 프로필은 `local`
- H2 in-memory DB 사용
- 시작 시 sample CSV 강의 데이터와 demo user/match/chat 데이터가 seed 된다.

## 주요 접속 경로
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`
- WebSocket endpoint: `ws://localhost:8080/ws-stomp`

## 환경 변수
- `.env.example` 참고
- 최소한 다음 값을 확인:
  - `FRONTEND_BASE_URL`
  - `JWT_SECRET`
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`

## local demo data
- 샘플 유저 3명 이상
- 샘플 수업
- 샘플 시간표
- 샘플 매칭 글
- 일부 course/direct chat 메시지

## 주의
- admin import API는 local/dev 환경에서만 허용된다.
- crawler는 비활성화 상태여도 앱은 정상 동작한다.
