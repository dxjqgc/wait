package com.wait.server.dto;

import lombok.Data;

@Data
public class WsSendDTO {
    private Long conversationId;
    private String content;
}
