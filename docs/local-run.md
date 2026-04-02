# Local Run

## 환경
- Java 21
- Windows 기준 명령:
  - `.\gradlew.bat test`
  - `.\gradlew.bat bootRun`

## 기본 프로필
- 기본 프로필은 `local`
- PostgreSQL DB 사용 (기본값: `localhost:5432/techblog_db`)
- 시작 시 JSON 기반 강의 데이터(약 2300건)와 demo user/match/chat 데이터가 seed 된다.

## 테스트 프로필
- `test` 프로필은 H2 in-memory DB를 사용한다 (`./gradlew test` 실행 시 자동 적용).

## 로컬 PostgreSQL 실행 (Docker)
- `khunect-backend/` 디렉토리에서 `docker compose up db`로 PostgreSQL만 기동할 수 있다.
- 첫 기동 시 컨테이너에 빈 DB가 생성되고, 앱 시작 후 `CourseDataLoader`가 JSON에서 데이터를 자동 적재한다.
- 강의 데이터는 한 번만 적재되며, 재시작해도 중복 삽입되지 않는다.

## 주요 접속 경로
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`
- WebSocket endpoint: `ws://localhost:8080/ws-stomp`

## 환경 변수
- `.env.example` 참고
- 최소한 다음 값을 확인:
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`
  - `GOOGLE_REDIRECT_URI`
  - `FRONTEND_BASE_URL`
  - `AUTH_CALLBACK_PATH`

## Google OAuth2 로컬 설정
- Google Cloud Console의 OAuth redirect URI에 아래 값을 등록해야 한다.
  - `http://localhost:8080/login/oauth2/code/google`
- 로컬 기본 callback 조합:
  - backend redirect URI: `GOOGLE_REDIRECT_URI`
  - frontend callback: `FRONTEND_BASE_URL` + `AUTH_CALLBACK_PATH`
- 로그인 시작 URL:
  - `http://localhost:8080/oauth2/authorization/google`
- 로그인 성공 후 백엔드는 프론트로 `?code=`를 붙여 리다이렉트하고, 프론트는 `/api/auth/exchange`를 호출해야 한다.

## local demo data
- 샘플 유저 3명 이상
- 샘플 수업
- 샘플 시간표
- 샘플 매칭 글
- 일부 course/direct chat 메시지

## 주의
- admin import API는 local/dev 환경에서만 허용된다.
- crawler는 비활성화 상태여도 앱은 정상 동작한다.
