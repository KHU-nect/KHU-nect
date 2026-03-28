# frontend-smoke AGENTS.md

## Goal
이 폴더는 Khunect 백엔드 기능을 검증하기 위한 최소 테스트용 프론트엔드다.
실서비스 UI/디자인은 목표가 아니다.

## Hard constraints
- 이 폴더 밖의 파일은 수정하지 말 것.
- 특히 ../src, ../build.gradle, ../settings.gradle, ../application*.yml 등 백엔드 파일은 절대 수정하지 말 것.
- 디자인 작업은 하지 말 것.
- 컴포넌트 분해를 과하게 하지 말 것.
- Vite + Vanilla JavaScript로 구현할 것.
- TypeScript, React, Next.js, Tailwind를 사용하지 말 것.
- fetch 기반 REST 호출과 최소 STOMP 채팅 테스트만 구현할 것.
- 코드량을 최소화할 것.
- 테스트 편의성이 최우선이다.

## Functional scope
- Google 로그인 시작 버튼
- auth callback 에서 code 수신 후 /api/auth/exchange 호출
- access/refresh token localStorage 저장
- /api/auth/me 호출
- 수업 검색
- 시간표 추가/조회/삭제
- 수업 채팅방 입장/목록/메시지 조회/전송
- 매칭글 CRUD/수락
- 1:1 채팅방 목록/메시지 조회/전송
- 마이페이지 summary 조회

## API assumptions
- 백엔드 base URL은 기본 http://localhost:8080
- 프론트 dev 서버는 기본 http://localhost:5173
- 보호 API는 Authorization: Bearer {accessToken}
- refresh 실패 시 로그인 상태를 정리

## UX
- 페이지는 예뻐질 필요 없음
- 단일 페이지 또는 최소 라우트
- 각 기능마다 입력창, 버튼, 결과 JSON 출력 영역만 있으면 됨