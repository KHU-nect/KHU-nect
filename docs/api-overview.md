# API Overview

## OpenAPI
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/api-docs`

## 인증 흐름
- 인증은 Google OAuth2 -> 1회용 auth code -> `POST /api/auth/exchange` -> access/refresh token 발급 흐름으로 동작한다.
- `POST /api/auth/refresh`는 refresh token rotation을 적용한다.
- 보호된 REST API는 `Authorization: Bearer <accessToken>` 헤더가 필요하다.
- STOMP WebSocket은 `CONNECT` 시 `Authorization: Bearer <token>` 헤더로 JWT를 검증한다.

## 주요 REST Endpoint
- `GET /api/courses`
- `GET /api/courses/{courseId}`
- `POST /api/timetable`
- `GET /api/timetable/me`
- `DELETE /api/timetable/{entryId}`
- `GET /api/users/me`
- `PATCH /api/users/me`
- `POST /api/users/me/signup-completion`
- `GET /api/interests/me`
- `POST /api/interests/me`
- `DELETE /api/interests/me/{interestId}`
- `GET /api/mypage/summary`
- `POST /api/course-chat/rooms/enter`
- `GET /api/course-chat/rooms/me`
- `GET /api/course-chat/rooms/{roomId}/messages`
- `POST /api/match-posts`
- `GET /api/match-posts`
- `GET /api/match-posts/{id}`
- `PATCH /api/match-posts/{id}`
- `DELETE /api/match-posts/{id}`
- `POST /api/match-posts/{id}/accept`
- `GET /api/direct-chat/rooms/me`
- `GET /api/direct-chat/rooms/{roomId}/messages`
- `POST /api/admin/courses/import/csv`
- `POST /api/admin/courses/import/crawler`

## WebSocket/STOMP
- Endpoint: `/ws-stomp`
- App destination prefix: `/pub`
- Broker prefix: `/sub`

## Course Chat Destination
- Send: `/pub/course-chat/rooms/{roomId}/messages`
- Subscribe: `/sub/course-chat/rooms/{roomId}`

## Direct Chat Destination
- Send: `/pub/direct-chat/rooms/{roomId}/messages`
- Subscribe: `/sub/direct-chat/rooms/{roomId}`

## 응답 Shape 메모
- 모든 REST 응답은 `ApiResponse<T>` 포맷을 사용한다.
- 성공 시:
  - `success: true`
  - `data: ...`
- 실패 시:
  - `success: false`
  - `error.code`
  - `error.message`
  - `error.fieldErrors`

## 프론트에서 자주 쓰는 필드
- `MyProfileResponse`
  - `userId`, `email`, `nickname`, `major`, `studentNumber`, `signupCompleted`, `point`, `level`, `interests`
- `MyPageSummaryResponse`
  - `nickname`, `major`, `maskedStudentNumber`, `point`, `level`, `registeredCourseCount`, `successfulMatchCount`, `helpedCount`, `weeklyStats`
- `CourseChatRoomResponse`
  - `roomId`, `courseId`, `courseName`, `lastMessagePreview`, `lastMessageTime`, `unreadCount`
- `DirectChatRoomResponse`
  - `roomId`, `opponentUserId`, `opponentNickname`, `lastMessagePreview`, `lastMessageTime`
- `MatchPostResponse`
  - `id`, `authorUserId`, `authorNickname`, `preferredTimeText`, `locationText`, `content`, `category`, `status`, `acceptedByUserId`, `acceptedAt`
