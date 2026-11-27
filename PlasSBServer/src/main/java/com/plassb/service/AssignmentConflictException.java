package com.plassb.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AssignmentConflictException extends RuntimeException {

    public AssignmentConflictException(String message) {
        super(message);
    }
}