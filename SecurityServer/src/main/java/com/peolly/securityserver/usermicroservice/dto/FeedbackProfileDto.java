package com.peolly.securityserver.usermicroservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FeedbackProfileDto {
    private int evaluation;
    private String message;
    private String feedbackLink;
}
