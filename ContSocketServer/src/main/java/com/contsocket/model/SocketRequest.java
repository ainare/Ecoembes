package com.contsocket.model;

import java.time.LocalDate;

public record SocketRequest(
        String action,
        LocalDate date,
        AssignmentNotice notice
) {
}