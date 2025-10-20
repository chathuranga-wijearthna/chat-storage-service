package com.assignment.chatstorage.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ErrorCode {

    SESSION_NOT_FOUND("ERR_CS_SES_01", "Session not found");

    private final String code;
    private final String description;
}
