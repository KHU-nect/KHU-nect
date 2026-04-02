# frontend-smoke

Khunect 백엔드 기능 테스트용 Vite + Vanilla JS 프론트엔드입니다.

## Run

```bash
npm install
npm run dev
```

Build check:

```bash
npm run build
```

## 기본 정보

- Backend base URL: `http://34.47.66.208:8080`
- Frontend dev URL: `http://localhost:5173`
- OAuth callback 처리: `/auth/callback?code=...` 또는 현재 URL query의 `code` 자동 인식

## 화면 구성

1. `Auth`: Google OAuth 시작, code 교환, me/refresh/logout
2. `Signup/Profile`: 최초 가입 완료, 내 프로필 조회/수정
3. `Interest`: 조회/추가/삭제
4. `Course/Timetable`: 수업 검색/상세, 시간표 추가/조회/삭제
5. `Course Chat`: 채팅방 생성/입장, 방 목록, 메시지 조회, STOMP 송수신
6. `Match Posts`: 목록/생성/상세/수정/삭제/수락
7. `Direct Chat`: 방 목록, 메시지 조회, STOMP 송수신
8. `MyPage`: summary 조회
9. `Last API`: 마지막 API 요청/응답 상태 추적

## 구현 포인트

- 모든 API 응답은 `ApiResponse<T>` 원문을 그대로 확인할 수 있습니다.
- 실패 시 `payload.error.code`, `payload.error.message`를 바로 확인할 수 있습니다.
- 보호 API는 `Authorization: Bearer {accessToken}`을 자동 첨부합니다.
- access token 만료 시 `/api/auth/refresh` 자동 재시도 로직이 들어 있습니다.
- STOMP CONNECT/PUBLISH에도 access token 헤더를 사용합니다.
- 화면 상단 상태바에서 선택된 `courseId`, `roomId`, `matchPostId`, `directRoomId`를 추적하고 입력값에 자동 반영합니다.
