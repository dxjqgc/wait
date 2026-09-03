package com.wait.server.controller;

import com.wait.server.dto.MessageVO;
import com.wait.server.dto.WsSendDTO;
import com.wait.server.entity.ConversationEntity;
import com.wait.server.entity.MessageEntity;
import com.wait.server.entity.UserEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.service.ConversationService;
import com.wait.server.service.MessageService;
import com.wait.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload WsSendDTO dto, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        ConversationEntity c = conversationService.getById(dto.getConversationId());
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!"ACTIVE".equals(c.getState())) {
            // 接收方首次回复：触发激活
            if ("PENDING".equals(c.getState()) && c.getTargetId().equals(senderId)) {
                conversationService.replyActivate(c.getId(), senderId);
                c = conversationService.getById(c.getId());
            } else {
                throw new BusinessException(409, "conversation is not ACTIVE");
            }
        }
        MessageEntity m = messageService.send(c.getId(), senderId, dto.getContent());

        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(m, vo);

        // 推送给双方
        Long otherId = c.getInitiatorId().equals(senderId) ? c.getTargetId() : c.getInitiatorId();
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", vo);
        messagingTemplate.convertAndSendToUser(otherId.toString(), "/queue/messages", vo);
    }
}
