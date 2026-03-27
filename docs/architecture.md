# Khunect Backend Architecture

## 전체 도메인 구조
- `common`: 공통 응답, 예외, 설정, 보안, 감사, 유틸
- `auth`: Google OAuth2 로그인, 1회용 auth code, JWT 발급/재발급, 로그아웃
- `user`: 사용자 기본 정보, 프로필 설정, 마이페이지
- `interest`: 취미/관심사 관리
- `course`: 수업 검색, 수업 정보 조회
- `timetable`: 시간표 조회/추가/삭제
- `chat`: 수업 채팅방, 1:1 채팅, STOMP 메시징
- `match`: 매칭 게시글, 신청/수락/상태 관리
- `crawler`: 수업 데이터 import, 외부 소스 수집

## 인증 흐름
1. 사용자가 프론트엔드에서 Google 로그인 시작
2. 백엔드가 OAuth2 인증 성공 후 이메일 도메인이 `khu.ac.kr`인지 확인
3. 백엔드는 토큰 대신 1회용 auth code를 생성하고 프론트 콜백 URL로 리다이렉트
4. 프론트는 `/api/auth/exchange`에 auth code를 전달
5. 백엔드는 auth code를 검증한 뒤 access token과 refresh token을 발급
6. 이후 보호된 REST API와 WebSocket 연결은 JWT 기반으로 인증

## 채팅 구조
- 수업별 채팅방: 특정 강의에 속한 다대다 채팅
- 1:1 채팅방: 매칭 수락 후 생성되는 사용자 간 채팅
- 메시지 전송은 STOMP destination 기준으로 분리한다.
- WebSocket 연결 시 JWT를 검증하고 사용자 정보를 Principal로 연결한다.
- 메시지 저장과 읽음 처리, 최근 메시지 조회는 추후 도메인 구현 단계에서 추가한다.

## 주요 엔티티 목록
- `User`
- `UserProfile`
- `Interest`
- `Course`
- `Timetable`
- `TimetableCourse`
- `CourseChatRoom`
- `DirectChatRoom`
- `ChatMessage`
- `MatchPost`
- `MatchApplication`
- `RefreshToken`
- `AuthCode`

## API 큰 분류
- `/api/auth/*`: 로그인 콜백, auth code 교환, 토큰 재발급, 로그아웃
- `/api/users/*`: 내 정보, 프로필 설정, 마이페이지
- `/api/interests/*`: 관심사 조회/수정
- `/api/courses/*`: 수업 검색/상세
- `/api/timetables/*`: 시간표 관리
- `/api/matches/*`: 매칭 게시글/신청/수락
- `/api/chat/*`: 채팅방 목록, 이전 메시지 조회
- `/ws` + STOMP endpoints: 실시간 메시지 송수신

## 현재 1단계 범위
- 공통 응답/예외 포맷
- 감사 엔티티와 JPA auditing 설정
- 최소 보안 스켈레톤과 CORS 설정
- 프로필별 설정 파일 정리
- 도메인 기능은 아직 구현하지 않음
