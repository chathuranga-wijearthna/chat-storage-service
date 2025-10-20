package com.assignment.chatstorage.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ErrorCode {

    SESSION_NOT_FOUND("ERR_CS_SES_01", "Session not found"),
    // Rate limiting
    RATE_LIMIT_EXCEEDED("ERR_CS_RATE_01", "Too many requests");

    private final String code;
    private final String description;
}
