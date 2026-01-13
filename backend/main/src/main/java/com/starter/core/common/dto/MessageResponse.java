package com.starter.core.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple message response DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
