package com.wait.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private Long initiatorId;
    private String initiatorNickname;
    private String initiatorAvatar;
    private Long targetId;
    private String targetNickname;
    private String targetAvatar;
    private String greetingMsg;
    private String state;
    private LocalDateTime createdAt;
    private LocalDateTime lastMsgAt;
    private LocalDateTime initiatorReadAt;
    private LocalDateTime targetReadAt;
    /** 当前用户是否是接收方且尚未回复 */
    private Boolean iAmTargetPendingReply;
    /** 当前用户在会话中是否是发起方 */
    private Boolean iAmInitiator;
}
