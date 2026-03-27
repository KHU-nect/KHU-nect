package com.khunect.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
	HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "COMMON-400-1", "요청 본문을 읽을 수 없습니다."),
	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON-400-2", "요청 파라미터 타입이 올바르지 않습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-405", "허용되지 않은 HTTP 메서드입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-403", "접근 권한이 없습니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "요청한 리소스를 찾을 수 없습니다."),
	SIGNUP_ALREADY_COMPLETED(HttpStatus.CONFLICT, "USER-409-1", "이미 가입 완료된 사용자입니다."),
	SIGNUP_NOT_COMPLETED(HttpStatus.FORBIDDEN, "USER-403-1", "가입 완료 후 이용할 수 있습니다."),
	COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE-404-1", "강의를 찾을 수 없습니다."),
	TIMETABLE_ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMETABLE-404-1", "시간표 항목을 찾을 수 없습니다."),
	TIMETABLE_ENTRY_DUPLICATED(HttpStatus.CONFLICT, "TIMETABLE-409-1", "이미 시간표에 추가된 강의입니다."),
	COURSE_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE-CHAT-404-1", "수업 채팅방을 찾을 수 없습니다."),
	COURSE_CHAT_ROOM_NOT_JOINABLE(HttpStatus.FORBIDDEN, "COURSE-CHAT-403-1", "해당 수업의 시간표 등록 사용자만 입장할 수 있습니다."),
	COURSE_CHAT_ROOM_CREATION_REQUIRED(HttpStatus.NOT_FOUND, "COURSE-CHAT-404-2", "채팅방이 아직 생성되지 않았습니다."),
	COURSE_CHAT_ROOM_MEMBERSHIP_REQUIRED(HttpStatus.FORBIDDEN, "COURSE-CHAT-403-2", "채팅방 멤버만 접근할 수 있습니다."),
	MATCH_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-404-1", "매칭 게시글을 찾을 수 없습니다."),
	MATCH_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "MATCH-403-1", "매칭 게시글에 대한 권한이 없습니다."),
	MATCH_POST_SELF_ACCEPT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "MATCH-400-1", "본인 글은 수락할 수 없습니다."),
	MATCH_POST_NOT_OPEN(HttpStatus.CONFLICT, "MATCH-409-1", "OPEN 상태의 매칭 글만 수락할 수 있습니다."),
	DIRECT_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DIRECT-CHAT-404-1", "1:1 채팅방을 찾을 수 없습니다."),
	DIRECT_CHAT_ROOM_MEMBERSHIP_REQUIRED(HttpStatus.FORBIDDEN, "DIRECT-CHAT-403-1", "채팅방 참여자만 접근할 수 있습니다."),
	INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-1", "유효하지 않은 JWT 토큰입니다."),
	INTEREST_ALREADY_ADDED(HttpStatus.CONFLICT, "INTEREST-409-1", "이미 추가된 관심사입니다."),
	INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST-404-1", "관심사를 찾을 수 없습니다."),
	USER_INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST-404-2", "사용자 관심사 정보를 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
