package com.assignment.chatstorage.exception;

import lombok.Getter;

@Getter
public class CustomGlobalException extends RuntimeException {

    private final String code;

    public CustomGlobalException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.code = errorCode.getCode();
    }

    public CustomGlobalException(ErrorCode errorCode, Object... args) {
        super(args == null || args.length == 0 ? errorCode.getDescription() : String.format(errorCode.getDescription(), args));
        this.code = errorCode.getCode();
    }
}