package com.wait.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}
