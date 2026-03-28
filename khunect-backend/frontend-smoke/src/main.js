import "./styles.css";
import {
  api,
  clearTokens,
  exchangeCode,
  getAccessToken,
  getBaseUrl,
  getRefreshToken,
  logout,
  refreshTokens,
  setBaseUrl
} from "./api";
import { createChatClient } from "./chat";

const app = document.getElementById("app");

const state = {
  signupCompleted: null,
  me: null,
  selectedCourseId: "",
  selectedTimetableEntryId: "",
  selectedCourseRoomId: "",
  selectedMatchPostId: "",
  selectedDirectRoomId: "",
  courseWs: null,
  directWs: null,
  courseMessages: [],
  directMessages: []
};

app.innerHTML = `
  <header class="header">
    <div>
      <h1>Khunect Backend Smoke UI</h1>
      <p>백엔드 기능 테스트 전용 프론트. 모든 응답은 ApiResponse 원문(JSON)으로 확인합니다.</p>
    </div>
    <div>
      <div id="authBadge" class="badge">로그아웃</div>
      <div id="tokenSummary" class="small"></div>
    </div>
  </header>

  <section class="panel" id="overview">
    <h2>0) 테스트 상태</h2>
    <div class="state-grid">
      <div class="state-item">signupCompleted: <strong id="stSignup">unknown</strong></div>
      <div class="state-item">courseId: <strong id="stCourseId">-</strong></div>
      <div class="state-item">timetableEntryId: <strong id="stEntryId">-</strong></div>
      <div class="state-item">courseRoomId: <strong id="stCourseRoom">-</strong></div>
      <div class="state-item">matchPostId: <strong id="stMatchId">-</strong></div>
      <div class="state-item">directRoomId: <strong id="stDirectRoom">-</strong></div>
    </div>
  </section>

  <section class="panel" id="auth">
    <h2>1) Auth</h2>
    <div class="row">
      <label>Backend URL <input id="baseUrl" placeholder="http://localhost:8080" /></label>
      <button id="saveBaseUrl">URL 저장</button>
      <button id="startGoogle">Google 로그인 시작</button>
      <button id="exchangeFromQuery">callback code 교환</button>
    </div>
    <div class="row">
      <button id="authMe">GET /api/auth/me</button>
      <button id="authRefresh">POST /api/auth/refresh</button>
      <button id="authLogout">POST /api/auth/logout</button>
      <button id="authClear">로컬 토큰 삭제</button>
    </div>
    <pre id="authOut"></pre>
  </section>

  <section class="panel" id="user">
    <h2>2) Signup / Profile</h2>
    <div id="signupWarning" class="warn" hidden>signupCompleted=false 이므로 먼저 가입 완료를 진행하세요.</div>
    <div class="row">
      <label>nickname <input id="signupNickname" value="khunect_1" /></label>
      <label>major <input id="signupMajor" value="컴퓨터공학과" /></label>
      <label>studentNumber <input id="signupStudentNumber" value="2024123456" /></label>
      <button id="signupComplete">POST /api/users/me/signup-completion</button>
    </div>
    <div class="row">
      <button id="userMe">GET /api/users/me</button>
      <label>nickname <input id="profileNickname" placeholder="khunect_new" /></label>
      <label>major <input id="profileMajor" placeholder="소프트웨어융합학과" /></label>
      <button id="userPatch">PATCH /api/users/me</button>
    </div>
    <pre id="userOut"></pre>
  </section>

  <section class="panel" id="interest">
    <h2>3) Interest</h2>
    <div class="row">
      <button class="requires-signup" id="interestList">GET /api/interests/me</button>
      <label>name <input id="interestName" value="보드게임" /></label>
      <button class="requires-signup" id="interestAdd">POST /api/interests/me</button>
      <label>interestId <input id="interestId" placeholder="1" /></label>
      <button class="requires-signup" id="interestDelete">DELETE /api/interests/me/{interestId}</button>
    </div>
    <div id="interestListBox" class="list-box">목록 없음</div>
    <pre id="interestOut"></pre>
  </section>

  <section class="panel" id="course">
    <h2>4) Course / Timetable</h2>
    <div class="sub">
      <h3>Course Search</h3>
      <div class="row">
        <label>keyword <input id="courseKeyword" value="" placeholder="자료구조" /></label>
        <label>page <input id="coursePage" value="0" /></label>
        <label>size <input id="courseSize" value="10" /></label>
        <button id="courseSearch">GET /api/courses</button>
      </div>
      <div class="row">
        <label>courseId <input id="courseId" placeholder="courseId" /></label>
        <button id="courseDetail">GET /api/courses/{courseId}</button>
      </div>
      <div id="courseList" class="list-box">검색 결과 없음</div>
      <pre id="courseOut"></pre>
    </div>

    <div class="sub">
      <h3>Timetable</h3>
      <div class="row">
        <label>courseId <input id="ttCourseId" placeholder="courseId" /></label>
        <button class="requires-signup" id="ttAdd">POST /api/timetable</button>
        <button class="requires-signup" id="ttList">GET /api/timetable/me</button>
      </div>
      <div class="row">
        <label>entryId <input id="ttEntryId" placeholder="entryId" /></label>
        <button class="requires-signup" id="ttDelete">DELETE /api/timetable/{entryId}</button>
      </div>
      <div id="ttListBox" class="list-box">시간표 없음</div>
      <pre id="ttOut"></pre>
    </div>
  </section>

  <section class="panel" id="courseChat">
    <h2>5) Course Chat (방 생성/입장 + 대화창)</h2>
    <div class="chat-layout">
      <div class="sub">
        <h3>Room</h3>
        <div class="row">
          <label>courseId <input id="courseEnterCourseId" placeholder="courseId" /></label>
          <label><input id="createIfAbsent" type="checkbox" checked /> createIfAbsent</label>
          <button class="requires-signup" id="courseEnter">POST /api/course-chat/rooms/enter</button>
          <button class="requires-signup" id="courseRooms">GET /api/course-chat/rooms/me</button>
        </div>
        <div id="courseRoomList" class="list-box">채팅방 없음</div>
      </div>

      <div class="sub">
        <h3>Messages</h3>
        <div class="row">
          <label>roomId <input id="courseRoomId" placeholder="roomId" /></label>
          <label>beforeMessageId <input id="courseBeforeId" placeholder="optional" /></label>
          <label>size <input id="courseMsgSize" value="20" /></label>
          <button class="requires-signup" id="courseMsgLoad">GET messages</button>
        </div>
        <div class="row">
          <button class="requires-signup" id="courseWsConnect">WS Connect</button>
          <button id="courseWsDisconnect">WS Disconnect</button>
          <input id="courseWsInput" placeholder="메시지 내용(content)" />
          <button class="requires-signup" id="courseWsSend">WS Send</button>
        </div>
        <div id="courseMsgFeed" class="chat-feed">메시지 없음</div>
      </div>
    </div>
    <pre id="courseChatOut"></pre>
  </section>

  <section class="panel" id="match">
    <h2>6) Match Posts</h2>
    <div class="row">
      <label>page <input id="matchPage" value="0" /></label>
      <label>size <input id="matchSize" value="10" /></label>
      <label>status <input id="matchStatus" placeholder="OPEN" /></label>
      <button class="requires-signup" id="matchList">GET /api/match-posts</button>
    </div>
    <div class="row">
      <label>matchPostId <input id="matchId" placeholder="id" /></label>
      <button class="requires-signup" id="matchDetail">GET /api/match-posts/{id}</button>
      <button class="requires-signup" id="matchDelete">DELETE /api/match-posts/{id}</button>
      <button class="requires-signup" id="matchAccept">POST /api/match-posts/{id}/accept</button>
    </div>
    <div class="row">
      <label>preferredTimeText <input id="matchTime" value="화요일 오후" /></label>
      <label>locationText <input id="matchLocation" value="중도" /></label>
      <label>category <input id="matchCategory" value="스터디" /></label>
    </div>
    <div class="row">
      <label class="grow">content <input id="matchContent" value="같이 과제하실 분 구해요" /></label>
      <button class="requires-signup" id="matchCreate">POST /api/match-posts</button>
      <button class="requires-signup" id="matchPatch">PATCH /api/match-posts/{id}</button>
    </div>
    <div id="matchListBox" class="list-box">매칭 글 없음</div>
    <pre id="matchOut"></pre>
  </section>

  <section class="panel" id="directChat">
    <h2>7) Direct Chat</h2>
    <div class="chat-layout">
      <div class="sub">
        <h3>Rooms</h3>
        <div class="row">
          <button class="requires-signup" id="directRooms">GET /api/direct-chat/rooms/me</button>
          <span class="small">매칭 수락 성공 시 roomId 자동 반영</span>
        </div>
        <div id="directRoomList" class="list-box">채팅방 없음</div>
      </div>

      <div class="sub">
        <h3>Messages</h3>
        <div class="row">
          <label>roomId <input id="directRoomId" placeholder="roomId" /></label>
          <label>beforeMessageId <input id="directBeforeId" placeholder="optional" /></label>
          <label>size <input id="directMsgSize" value="20" /></label>
          <button class="requires-signup" id="directMsgLoad">GET messages</button>
        </div>
        <div class="row">
          <button class="requires-signup" id="directWsConnect">WS Connect</button>
          <button id="directWsDisconnect">WS Disconnect</button>
          <input id="directWsInput" placeholder="메시지 내용(content)" />
          <button class="requires-signup" id="directWsSend">WS Send</button>
        </div>
        <div id="directMsgFeed" class="chat-feed">메시지 없음</div>
      </div>
    </div>
    <pre id="directOut"></pre>
  </section>

  <section class="panel" id="mypage">
    <h2>8) MyPage</h2>
    <button class="requires-signup" id="mypageSummary">GET /api/mypage/summary</button>
    <pre id="mypageOut"></pre>
  </section>

  <section class="panel" id="logPanel">
    <h2>9) Last API</h2>
    <pre id="lastApiOut">요청 전</pre>
  </section>
`;

const $ = (id) => document.getElementById(id);

function read(id) {
  return $(id).value.trim();
}

function toLong(value) {
  if (!value) return null;
  const num = Number(value);
  return Number.isNaN(num) ? null : num;
}

function setJson(id, value) {
  $(id).textContent = JSON.stringify(value, null, 2);
}

function extractData(result) {
  return result?.payload?.data ?? result?.data;
}

function extractError(result) {
  const payload = result?.payload;
  if (!payload || typeof payload !== "object") return null;
  return payload.error || null;
}

function extractList(data) {
  if (Array.isArray(data)) return data;
  if (!data || typeof data !== "object") return [];
  if (Array.isArray(data.content)) return data.content;
  if (Array.isArray(data.messages)) return data.messages;
  return [];
}

function resolveSignup(data) {
  if (!data || typeof data !== "object") return null;
  if (typeof data.signupCompleted === "boolean") return data.signupCompleted;
  if (data.user && typeof data.user.signupCompleted === "boolean") return data.user.signupCompleted;
  return null;
}

function renderList(targetId, rows, textFn, onClick) {
  const box = $(targetId);
  box.innerHTML = "";
  if (!rows.length) {
    box.textContent = "결과 없음";
    return;
  }
  rows.forEach((row) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "list-item";
    button.textContent = textFn(row);
    button.onclick = () => onClick?.(row);
    box.appendChild(button);
  });
}

function renderMessages(targetId, messages) {
  const feed = $(targetId);
  feed.innerHTML = "";
  if (!messages.length) {
    feed.textContent = "메시지 없음";
    return;
  }
  [...messages]
    .sort((a, b) => (a.messageId || 0) - (b.messageId || 0))
    .forEach((m) => {
      const div = document.createElement("div");
      div.className = "msg";
      const nick = m.senderNickname || m.writer || "unknown";
      const body = m.content || m.message || "";
      div.textContent = `#${m.messageId ?? "-"} [${nick}] ${body} ${m.createdAt ? `(${m.createdAt})` : ""}`;
      feed.appendChild(div);
    });
  feed.scrollTop = feed.scrollHeight;
}

function mergeMessages(prev, incoming) {
  const map = new Map();
  [...prev, ...incoming].forEach((m) => {
    if (m?.messageId != null) {
      map.set(m.messageId, m);
    }
  });
  return [...map.values()];
}

function updateStateView() {
  $("stSignup").textContent =
    state.signupCompleted === null ? "unknown" : state.signupCompleted ? "true" : "false";
  $("stCourseId").textContent = state.selectedCourseId || "-";
  $("stEntryId").textContent = state.selectedTimetableEntryId || "-";
  $("stCourseRoom").textContent = state.selectedCourseRoomId || "-";
  $("stMatchId").textContent = state.selectedMatchPostId || "-";
  $("stDirectRoom").textContent = state.selectedDirectRoomId || "-";

  const blocked = state.signupCompleted === false;
  $("signupWarning").hidden = !blocked;
  document.querySelectorAll(".requires-signup").forEach((el) => {
    el.disabled = blocked;
  });

  const loggedIn = !!getAccessToken();
  const badge = $("authBadge");
  badge.textContent = loggedIn ? "로그인" : "로그아웃";
  badge.className = `badge ${loggedIn ? "ok" : ""}`;
  $("tokenSummary").textContent = `baseUrl=${getBaseUrl()} | access=${loggedIn ? "set" : "empty"} | refresh=${
    getRefreshToken() ? "set" : "empty"
  }`;
}

function fillLinkedInputs() {
  if (state.selectedCourseId) {
    $("courseId").value = state.selectedCourseId;
    $("ttCourseId").value = state.selectedCourseId;
    $("courseEnterCourseId").value = state.selectedCourseId;
  }
  if (state.selectedTimetableEntryId) {
    $("ttEntryId").value = state.selectedTimetableEntryId;
  }
  if (state.selectedCourseRoomId) {
    $("courseRoomId").value = state.selectedCourseRoomId;
  }
  if (state.selectedMatchPostId) {
    $("matchId").value = state.selectedMatchPostId;
  }
  if (state.selectedDirectRoomId) {
    $("directRoomId").value = state.selectedDirectRoomId;
  }
}

function trackResult(result) {
  const data = extractData(result);
  const error = extractError(result);

  const nextSignup = resolveSignup(data);
  if (typeof nextSignup === "boolean") {
    state.signupCompleted = nextSignup;
  }

  if (data && typeof data === "object") {
    if (data.courseId) state.selectedCourseId = String(data.courseId);
    if (data.entryId) state.selectedTimetableEntryId = String(data.entryId);
    if (data.roomId) state.selectedCourseRoomId = String(data.roomId);
    if (data.id && /\/api\/match-posts/.test(lastPath)) state.selectedMatchPostId = String(data.id);

    if (data.directRoomId) state.selectedDirectRoomId = String(data.directRoomId);
    if (data.directChatRoomId) state.selectedDirectRoomId = String(data.directChatRoomId);

    if (data.nickname || data.email || data.userId || data.id) {
      state.me = data.user || data;
    }
  }

  setJson("lastApiOut", {
    method: lastMethod,
    path: lastPath,
    status: result?.status,
    ok: result?.ok,
    errorCode: error?.code || result?.errorCode || null,
    errorMessage: error?.message || null
  });

  if (error?.code) {
    console.warn("API ERROR", error.code, error.message);
  }

  updateStateView();
  fillLinkedInputs();
}

let lastMethod = "";
let lastPath = "";

async function call(outId, method, path, requestFn) {
  lastMethod = method;
  lastPath = path;
  const result = await requestFn();
  setJson(outId, {
    status: result?.status,
    ok: result?.ok,
    payload: result?.payload,
    error: extractError(result)
  });
  trackResult(result);
  return result;
}

function closeWs(client) {
  if (client) client.deactivate();
}

function bindAuth() {
  $("baseUrl").value = getBaseUrl();

  $("saveBaseUrl").onclick = () => {
    setBaseUrl(read("baseUrl"));
    updateStateView();
  };

  $("startGoogle").onclick = () => {
    window.location.href = `${getBaseUrl()}/oauth2/authorization/google`;
  };

  $("exchangeFromQuery").onclick = async () => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    const error = params.get("error");
    if (error) {
      setJson("authOut", { callbackError: error });
      return;
    }
    if (!code) {
      setJson("authOut", { message: "query string에 code가 없습니다." });
      return;
    }
    const result = await call("authOut", "POST", "/api/auth/exchange", () => exchangeCode(code));
    const data = extractData(result);
    if (data && typeof data === "object" && data.user) {
      state.me = data.user;
      if (typeof data.signupCompleted === "boolean") {
        state.signupCompleted = data.signupCompleted;
      }
      updateStateView();
      fillLinkedInputs();
    }
  };

  $("authMe").onclick = () => call("authOut", "GET", "/api/auth/me", () => api.get("/api/auth/me"));

  $("authRefresh").onclick = async () => {
    const ok = await refreshTokens();
    setJson("authOut", { refreshSuccess: ok });
    updateStateView();
  };

  $("authLogout").onclick = () => call("authOut", "POST", "/api/auth/logout", () => logout());

  $("authClear").onclick = () => {
    clearTokens();
    state.signupCompleted = null;
    state.me = null;
    updateStateView();
    setJson("authOut", { cleared: true });
  };
}

function bindUser() {
  $("signupComplete").onclick = () =>
    call("userOut", "POST", "/api/users/me/signup-completion", () =>
      api.post("/api/users/me/signup-completion", {
        nickname: read("signupNickname"),
        major: read("signupMajor"),
        studentNumber: read("signupStudentNumber")
      })
    );

  $("userMe").onclick = () => call("userOut", "GET", "/api/users/me", () => api.get("/api/users/me"));

  $("userPatch").onclick = () =>
    call("userOut", "PATCH", "/api/users/me", () =>
      api.patch("/api/users/me", {
        nickname: read("profileNickname") || read("signupNickname"),
        major: read("profileMajor") || read("signupMajor")
      })
    );
}

function bindInterest() {
  $("interestList").onclick = async () => {
    const result = await call("interestOut", "GET", "/api/interests/me", () => api.get("/api/interests/me"));
    const list = extractList(extractData(result));
    renderList(
      "interestListBox",
      list,
      (i) => `${i.interestId ?? i.id} | ${i.name}`,
      (i) => {
        $("interestId").value = String(i.interestId ?? i.id ?? "");
      }
    );
  };

  $("interestAdd").onclick = () =>
    call("interestOut", "POST", "/api/interests/me", () =>
      api.post("/api/interests/me", {
        name: read("interestName")
      })
    );

  $("interestDelete").onclick = () =>
    call("interestOut", "DELETE", `/api/interests/me/${read("interestId")}`, () =>
      api.delete(`/api/interests/me/${read("interestId")}`)
    );
}

function bindCourseAndTimetable() {
  $("courseSearch").onclick = async () => {
    const keyword = encodeURIComponent(read("courseKeyword"));
    const page = encodeURIComponent(read("coursePage") || "0");
    const size = encodeURIComponent(read("courseSize") || "10");
    const path = `/api/courses?keyword=${keyword}&page=${page}&size=${size}`;

    const result = await call("courseOut", "GET", path, () => api.get(path, false));
    const data = extractData(result);
    const rows = extractList(data);
    renderList(
      "courseList",
      rows,
      (c) => {
        const id = c.id ?? c.courseId;
        return `${id} | ${c.courseName} | ${c.professorName ?? "-"} | ${c.scheduleText ?? "-"}`;
      },
      (c) => {
        state.selectedCourseId = String(c.id ?? c.courseId ?? "");
        updateStateView();
        fillLinkedInputs();
      }
    );
  };

  $("courseDetail").onclick = () =>
    call("courseOut", "GET", `/api/courses/${read("courseId")}`, () => api.get(`/api/courses/${read("courseId")}`, false));

  $("ttAdd").onclick = () =>
    call("ttOut", "POST", "/api/timetable", () =>
      api.post("/api/timetable", {
        courseId: toLong(read("ttCourseId"))
      })
    );

  $("ttList").onclick = async () => {
    const result = await call("ttOut", "GET", "/api/timetable/me", () => api.get("/api/timetable/me"));
    const rows = extractList(extractData(result));
    renderList(
      "ttListBox",
      rows,
      (t) => `${t.entryId} | ${t.courseId} | ${t.courseName} | ${t.scheduleText ?? "-"}`,
      (t) => {
        state.selectedTimetableEntryId = String(t.entryId ?? "");
        if (t.courseId) state.selectedCourseId = String(t.courseId);
        updateStateView();
        fillLinkedInputs();
      }
    );
  };

  $("ttDelete").onclick = () =>
    call("ttOut", "DELETE", `/api/timetable/${read("ttEntryId")}`, () => api.delete(`/api/timetable/${read("ttEntryId")}`));
}

function extractMessageList(historyData) {
  if (!historyData || typeof historyData !== "object") return [];
  if (Array.isArray(historyData.content)) return historyData.content;
  if (Array.isArray(historyData.messages)) return historyData.messages;
  return [];
}

function bindCourseChat() {
  $("courseEnter").onclick = async () => {
    const result = await call("courseChatOut", "POST", "/api/course-chat/rooms/enter", () =>
      api.post("/api/course-chat/rooms/enter", {
        courseId: toLong(read("courseEnterCourseId")),
        createIfAbsent: $("createIfAbsent").checked
      })
    );

    const room = extractData(result);
    if (room?.roomId) {
      state.selectedCourseRoomId = String(room.roomId);
      updateStateView();
      fillLinkedInputs();
    }
  };

  $("courseRooms").onclick = async () => {
    const result = await call("courseChatOut", "GET", "/api/course-chat/rooms/me", () =>
      api.get("/api/course-chat/rooms/me")
    );
    const rooms = extractList(extractData(result));
    renderList(
      "courseRoomList",
      rooms,
      (r) => `${r.roomId} | ${r.courseName} | unread:${r.unreadCount ?? 0}`,
      (r) => {
        state.selectedCourseRoomId = String(r.roomId ?? "");
        if (r.courseId) state.selectedCourseId = String(r.courseId);
        updateStateView();
        fillLinkedInputs();
      }
    );
  };

  $("courseMsgLoad").onclick = async () => {
    const roomId = read("courseRoomId");
    const before = read("courseBeforeId");
    const size = read("courseMsgSize") || "20";
    const query = before ? `?beforeMessageId=${encodeURIComponent(before)}&size=${encodeURIComponent(size)}` : `?size=${encodeURIComponent(size)}`;

    const path = `/api/course-chat/rooms/${roomId}/messages${query}`;
    const result = await call("courseChatOut", "GET", path, () => api.get(path));
    const data = extractData(result);
    const incoming = extractMessageList(data);
    state.courseMessages = mergeMessages(state.courseMessages, incoming);
    renderMessages("courseMsgFeed", state.courseMessages);
  };

  $("courseWsConnect").onclick = () => {
    closeWs(state.courseWs);
    state.courseWs = createChatClient(
      "course",
      read("courseRoomId"),
      (msg) => {
        state.courseMessages = mergeMessages(state.courseMessages, [msg]);
        renderMessages("courseMsgFeed", state.courseMessages);
        setJson("courseChatOut", { wsMessage: msg });
      },
      (status) => {
        setJson("courseChatOut", { wsStatus: status });
      }
    );
    state.courseWs.activate();
  };

  $("courseWsDisconnect").onclick = () => closeWs(state.courseWs);

  $("courseWsSend").onclick = () => {
    if (!state.courseWs) {
      setJson("courseChatOut", { error: "먼저 WS Connect 필요" });
      return;
    }
    state.courseWs.publish({ content: read("courseWsInput") });
    $("courseWsInput").value = "";
  };
}

function buildMatchBody() {
  return {
    preferredTimeText: read("matchTime"),
    locationText: read("matchLocation"),
    content: read("matchContent"),
    category: read("matchCategory") || null
  };
}

function bindMatch() {
  $("matchList").onclick = async () => {
    const page = read("matchPage") || "0";
    const size = read("matchSize") || "10";
    const status = read("matchStatus");
    const query = status ? `?status=${encodeURIComponent(status)}&page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}` : `?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
    const path = `/api/match-posts${query}`;

    const result = await call("matchOut", "GET", path, () => api.get(path));
    const posts = extractList(extractData(result));
    renderList(
      "matchListBox",
      posts,
      (p) => `${p.id} | ${p.status} | ${p.preferredTimeText} | ${p.locationText}`,
      (p) => {
        state.selectedMatchPostId = String(p.id ?? "");
        updateStateView();
        fillLinkedInputs();
      }
    );
  };

  $("matchCreate").onclick = () =>
    call("matchOut", "POST", "/api/match-posts", () => api.post("/api/match-posts", buildMatchBody()));

  $("matchDetail").onclick = () =>
    call("matchOut", "GET", `/api/match-posts/${read("matchId")}`, () => api.get(`/api/match-posts/${read("matchId")}`));

  $("matchPatch").onclick = () =>
    call("matchOut", "PATCH", `/api/match-posts/${read("matchId")}`, () =>
      api.patch(`/api/match-posts/${read("matchId")}`, buildMatchBody())
    );

  $("matchDelete").onclick = () =>
    call("matchOut", "DELETE", `/api/match-posts/${read("matchId")}`, () =>
      api.delete(`/api/match-posts/${read("matchId")}`)
    );

  $("matchAccept").onclick = async () => {
    const result = await call("matchOut", "POST", `/api/match-posts/${read("matchId")}/accept`, () =>
      api.post(`/api/match-posts/${read("matchId")}/accept`, {})
    );
    const data = extractData(result);
    const directRoomId = data?.directRoomId || data?.directChatRoomId;
    if (directRoomId) {
      state.selectedDirectRoomId = String(directRoomId);
      updateStateView();
      fillLinkedInputs();
    }
  };
}

function bindDirectChat() {
  $("directRooms").onclick = async () => {
    const result = await call("directOut", "GET", "/api/direct-chat/rooms/me", () =>
      api.get("/api/direct-chat/rooms/me")
    );
    const rooms = extractList(extractData(result));
    renderList(
      "directRoomList",
      rooms,
      (r) => `${r.roomId} | ${r.opponentNickname} | ${r.lastMessagePreview ?? ""}`,
      (r) => {
        state.selectedDirectRoomId = String(r.roomId ?? "");
        updateStateView();
        fillLinkedInputs();
      }
    );
  };

  $("directMsgLoad").onclick = async () => {
    const roomId = read("directRoomId");
    const before = read("directBeforeId");
    const size = read("directMsgSize") || "20";
    const query = before ? `?beforeMessageId=${encodeURIComponent(before)}&size=${encodeURIComponent(size)}` : `?size=${encodeURIComponent(size)}`;

    const path = `/api/direct-chat/rooms/${roomId}/messages${query}`;
    const result = await call("directOut", "GET", path, () => api.get(path));
    const incoming = extractMessageList(extractData(result));
    state.directMessages = mergeMessages(state.directMessages, incoming);
    renderMessages("directMsgFeed", state.directMessages);
  };

  $("directWsConnect").onclick = () => {
    closeWs(state.directWs);
    state.directWs = createChatClient(
      "direct",
      read("directRoomId"),
      (msg) => {
        state.directMessages = mergeMessages(state.directMessages, [msg]);
        renderMessages("directMsgFeed", state.directMessages);
        setJson("directOut", { wsMessage: msg });
      },
      (status) => {
        setJson("directOut", { wsStatus: status });
      }
    );
    state.directWs.activate();
  };

  $("directWsDisconnect").onclick = () => closeWs(state.directWs);

  $("directWsSend").onclick = () => {
    if (!state.directWs) {
      setJson("directOut", { error: "먼저 WS Connect 필요" });
      return;
    }
    state.directWs.publish({ content: read("directWsInput") });
    $("directWsInput").value = "";
  };
}

function bindMyPage() {
  $("mypageSummary").onclick = () => call("mypageOut", "GET", "/api/mypage/summary", () => api.get("/api/mypage/summary"));
}

bindAuth();
bindUser();
bindInterest();
bindCourseAndTimetable();
bindCourseChat();
bindMatch();
bindDirectChat();
bindMyPage();
updateStateView();
fillLinkedInputs();

if (window.location.pathname === "/auth/callback" || window.location.search.includes("code=")) {
  $("exchangeFromQuery").click();
}
