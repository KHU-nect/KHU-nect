# Auth Flow

## Login Flow
1. Frontend sends the user to `/oauth2/authorization/google`.
2. Google login succeeds.
3. Backend allows only `@khu.ac.kr` email accounts.
4. Backend creates a one-time auth code with short expiration.
5. Backend redirects to `{FRONTEND_BASE_URL}/auth/callback?code={authCode}`.

Required env for local run:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google`
- `FRONTEND_BASE_URL=http://localhost:5173`
- `AUTH_CALLBACK_PATH=/auth/callback`

Failure example:

`{FRONTEND_BASE_URL}/auth/callback?error=AUTH-403-1`

## Exchange

`POST /api/auth/exchange`

```json
{
  "code": "one-time-auth-code"
}
```

Returns `accessToken`, `refreshToken`, `signupCompleted`, and the minimum user summary.

## Refresh

`POST /api/auth/refresh`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Refresh token rotation is applied. The previous token is revoked immediately.

## Logout

`POST /api/auth/logout`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

The matching refresh token is revoked.

## Me

`GET /api/auth/me`

Header:

`Authorization: Bearer {accessToken}`

## Authorization Header

Protected REST APIs require:

`Authorization: Bearer {accessToken}`

STOMP `CONNECT` must also send the same access token in the native `Authorization` header.
