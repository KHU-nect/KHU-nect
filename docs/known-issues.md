# Known Issues

## 인증/보안
- HTTP JWT 인증 필터, OAuth2 로그인 성공 후 auth code 교환, refresh token 저장/회전은 아직 완성되지 않았다.
- 현재 테스트는 `MockMvc user()` 기반이며, 실제 REST 토큰 인증 흐름은 다음 단계 구현이 필요하다.
- STOMP는 `CONNECT` 시 JWT를 검증하지만, HTTP와 완전히 동일한 인증 체계로 통합되지는 않았다.

## 조회 성능
- course chat room list, direct chat room list는 각 방의 마지막 메시지를 조회할 때 추가 쿼리가 발생할 수 있다.
- unread count는 아직 계산하지 않고 기본값으로 응답한다.

## 크롤러
- KHU course crawler는 config + parser + fixture test 구조까지만 고정됐다.
- 실제 경희대 검색 엔드포인트, 파라미터, selector는 live 확인 후 보정이 필요하다.

## 기타
- `getOrCreateUser`는 현재 스텁 인증 단계 편의를 위한 동작이며, 실제 인증 도입 후 정책 재검토가 필요하다.
