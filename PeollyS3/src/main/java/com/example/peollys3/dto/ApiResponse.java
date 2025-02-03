package com.example.peollys3.dto;

import java.time.LocalDateTime;

public record ApiResponse(boolean success, String message) {
    public String getTimeStamp() {
        return LocalDateTime.now().toString();
    }
}