const DEFAULT_BASE_URL = "http://localhost:8080";
const BASE_URL_KEY = "khunect.baseUrl";
const ACCESS_TOKEN_KEY = "khunect.accessToken";
const REFRESH_TOKEN_KEY = "khunect.refreshToken";

let refreshInFlight = null;

export function getBaseUrl() {
  return localStorage.getItem(BASE_URL_KEY) || DEFAULT_BASE_URL;
}

export function setBaseUrl(url) {
  localStorage.setItem(BASE_URL_KEY, (url || DEFAULT_BASE_URL).trim());
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || "";
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || "";
}

export function saveTokens(tokens = {}) {
  const accessToken = tokens.accessToken || tokens.access_token || "";
  const refreshToken = tokens.refreshToken || tokens.refresh_token || "";
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

function getErrorCode(payload) {
  if (!payload || typeof payload !== "object") return "";
  return (
    payload.code ||
    payload.errorCode ||
    payload.error?.code ||
    payload.data?.code ||
    ""
  );
}

function getApiData(payload) {
  if (payload && typeof payload === "object" && "data" in payload) {
    return payload.data;
  }
  return payload;
}

function looksLikeAuthError(status, payload) {
  if (status === 401) return true;
  const code = String(getErrorCode(payload) || "").toUpperCase();
  return code.includes("TOKEN") || code.includes("UNAUTHORIZED") || code.includes("AUTH");
}

async function parseJson(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

function absoluteUrl(path) {
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  return `${getBaseUrl()}${path}`;
}

async function send(path, options = {}) {
  const {
    method = "GET",
    headers = {},
    body,
    auth = true,
    retryOnAuthError = true
  } = options;

  const finalHeaders = { ...headers };
  const accessToken = getAccessToken();
  if (auth && accessToken) {
    finalHeaders.Authorization = `Bearer ${accessToken}`;
  }

  let requestBody = body;
  if (body && typeof body === "object" && !(body instanceof FormData)) {
    finalHeaders["Content-Type"] = finalHeaders["Content-Type"] || "application/json";
    requestBody = JSON.stringify(body);
  }

  const res = await fetch(absoluteUrl(path), {
    method,
    headers: finalHeaders,
    body: requestBody
  });
  const payload = await parseJson(res);

  if (auth && retryOnAuthError && looksLikeAuthError(res.status, payload)) {
    const refreshed = await refreshTokens();
    if (refreshed) {
      return send(path, { ...options, retryOnAuthError: false });
    }
  }

  return {
    ok: res.ok,
    status: res.status,
    payload,
    data: getApiData(payload),
    errorCode: getErrorCode(payload)
  };
}

function extractTokensFromPayload(payload) {
  const data = getApiData(payload);
  if (!data || typeof data !== "object") return null;
  const accessToken = data.accessToken || data.access_token;
  const refreshToken = data.refreshToken || data.refresh_token;
  if (!accessToken && !refreshToken) return null;
  return { accessToken, refreshToken };
}

async function doRefresh() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearTokens();
    return false;
  }
  const result = await send("/api/auth/refresh", {
    method: "POST",
    auth: false,
    retryOnAuthError: false,
    body: { refreshToken }
  });
  if (!result.ok) {
    clearTokens();
    return false;
  }
  const nextTokens = extractTokensFromPayload(result.payload);
  if (!nextTokens?.accessToken) {
    clearTokens();
    return false;
  }
  saveTokens(nextTokens);
  return true;
}

export async function refreshTokens() {
  if (!refreshInFlight) {
    refreshInFlight = doRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

export async function exchangeCode(code) {
  const result = await send("/api/auth/exchange", {
    method: "POST",
    auth: false,
    body: { code }
  });
  if (result.ok) {
    const tokens = extractTokensFromPayload(result.payload);
    if (tokens) saveTokens(tokens);
  }
  return result;
}

export async function logout() {
  const refreshToken = getRefreshToken();
  const result = await send("/api/auth/logout", {
    method: "POST",
    body: refreshToken ? { refreshToken } : {}
  });
  clearTokens();
  return result;
}

export const api = {
  get: (path, auth = true) => send(path, { method: "GET", auth }),
  post: (path, body, auth = true) => send(path, { method: "POST", body, auth }),
  patch: (path, body, auth = true) => send(path, { method: "PATCH", body, auth }),
  delete: (path, auth = true) => send(path, { method: "DELETE", auth })
};
