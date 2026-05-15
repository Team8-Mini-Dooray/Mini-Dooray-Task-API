package com.nhnacademy.taskAPI.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    MISSING_USER_ID(HttpStatus.BAD_REQUEST, "MISSING_USER_ID", "X-User-Id Header가 없거나 빈 값입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문 값이 유효하지 않습니다."),
    INVALID_PROJECT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_PROJECT_STATUS", "사용할 수 없는 프로젝트 상태입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "시작일은 종료일보다 늦을 수 없습니다."),

    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."),
    MILESTONE_NOT_FOUND(HttpStatus.NOT_FOUND, "MILESTONE_NOT_FOUND", "마일스톤을 찾을 수 없습니다."),

    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "NOT_PROJECT_MEMBER", "요청자가 프로젝트 멤버가 아닙니다."),
    NOT_PROJECT_ADMIN(HttpStatus.FORBIDDEN, "NOT_PROJECT_ADMIN", "요청자가 프로젝트 관리자가 아닙니다."),

    DUPLICATE_PROJECT_MEMBER(HttpStatus.CONFLICT, "DUPLICATE_PROJECT_MEMBER", "이미 등록된 프로젝트 멤버입니다."),
    DUPLICATE_MILESTONE_NAME(HttpStatus.CONFLICT, "DUPLICATE_MILESTONE_NAME", "같은 프로젝트에 동일한 마일스톤 이름이 존재합니다."),

    PROJECT_NOT_ACTIVE(HttpStatus.CONFLICT, "PROJECT_NOT_ACTIVE", "종료 상태의 프로젝트에서는 생성/수정/삭제 작업을 할 수 없습니다."),
    ADMIN_MEMBER_CANNOT_BE_REMOVED(HttpStatus.CONFLICT, "ADMIN_MEMBER_CANNOT_BE_REMOVED", "프로젝트 관리자는 멤버에서 삭제할 수 없습니다."),

    TASK_TAG_REQUIRED(HttpStatus.BAD_REQUEST, "TASK_TAG_REQUIRED", "Task는 1개 이상의 Tag가 필요합니다."),
    TAG_NOT_IN_PROJECT(HttpStatus.BAD_REQUEST, "TAG_NOT_IN_PROJECT", "해당 태그가 요청한 프로젝트 소속이 아닙니다."),
    MILESTONE_NOT_IN_PROJECT(HttpStatus.BAD_REQUEST, "MILESTONE_NOT_IN_PROJECT", "해당 마일스톤이 요청한 프로젝트 소속이 아닙니다."),
    TASK_NOT_IN_PROJECT(HttpStatus.BAD_REQUEST, "TASK_NOT_IN_PROJECT", "해당 Task가 요청한 프로젝트 소속이 아닙니다."),
    COMMENT_NOT_IN_TASK(HttpStatus.BAD_REQUEST, "COMMENT_NOT_IN_TASK", "해당 댓글이 요청한 Task 소속이 아닙니다."),

    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", "태그를 찾을 수 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "Task를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),

    NOT_COMMENT_WRITER(HttpStatus.FORBIDDEN, "NOT_COMMENT_WRITER", "요청자가 댓글 작성자가 아닙니다."),

    DUPLICATE_TAG_NAME(HttpStatus.CONFLICT, "DUPLICATE_TAG_NAME", "같은 프로젝트에 동일한 태그 이름이 존재합니다.");

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

    public int getStatusValue() {
        return status.value();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}