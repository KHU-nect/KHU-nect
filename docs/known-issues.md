# Known Issues

## 인증/보안
- OAuth2 로그인은 Google 기준으로 구현되어 있으며, 운영 반영 전에는 실제 Google Console redirect URI와 프론트 callback URL을 맞춰야 한다.
- refresh token은 해시 저장 + rotation 구조지만, 다중 기기 세션 관리 정책은 아직 세분화하지 않았다.
- 일부 기존 테스트는 여전히 `MockMvc user()`를 함께 사용한다. 실제 인증 검증은 auth 통합 테스트에서 Bearer 토큰으로 보완한다.

## 조회 성능
- course chat room list, direct chat room list는 각 방의 마지막 메시지를 조회할 때 추가 쿼리가 발생할 수 있다.
- unread count는 아직 계산하지 않고 기본값으로 응답한다.

## 크롤러
- KHU course crawler는 config + parser + fixture test 구조까지만 고정됐다.
- 실제 경희대 검색 엔드포인트, 파라미터, selector는 live 확인 후 보정이 필요하다.

## 기타
- `getOrCreateUser`는 OAuth2 첫 로그인 사용자 생성과 기존 일부 서비스 경로에서 함께 사용된다. 향후에는 인증 이후 자동 생성이 불필요한 경로를 분리할 수 있다.
