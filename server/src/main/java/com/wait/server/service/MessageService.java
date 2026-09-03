package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wait.server.entity.MessageEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MessageMapper, MessageEntity> {

    private final ConversationService conversationService;
    private final UserService userService;

    @Transactional
    public MessageEntity send(Long conversationId, Long senderId, String content) {
        var c = conversationService.getById(conversationId);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!"ACTIVE".equals(c.getState())) {
            throw new BusinessException(409, "conversation is not ACTIVE");
        }
        if (!c.getInitiatorId().equals(senderId) && !c.getTargetId().equals(senderId)) {
            throw new BusinessException(403, "not a participant");
        }
        MessageEntity m = new MessageEntity();
        m.setConversationId(conversationId);
        m.setSenderId(senderId);
        m.setContent(content);
        m.setCreatedAt(LocalDateTime.now());
        save(m);

        // 更新会话最后消息时间
        c.setLastMsgAt(m.getCreatedAt());
        if (c.getInitiatorId().equals(senderId)) {
            c.setInitiatorReadAt(m.getCreatedAt());
        } else {
            c.setTargetReadAt(m.getCreatedAt());
        }
        conversationService.updateById(c);

        // 触发活跃时间
        var sender = userService.getById(senderId);
        if (sender != null) {
            sender.setLastActiveAt(m.getCreatedAt());
            userService.updateById(sender);
        }
        return m;
    }

    public List<MessageEntity> list(Long conversationId, Long userId, Long beforeId, int limit) {
        var c = conversationService.getById(conversationId);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!c.getInitiatorId().equals(userId) && !c.getTargetId().equals(userId)) {
            throw new BusinessException(403, "not a participant");
        }
        LambdaQueryWrapper<MessageEntity> w = new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, conversationId)
                .orderByDesc(MessageEntity::getId)
                .last("LIMIT " + Math.min(Math.max(limit, 1), 100));
        if (beforeId != null) w.lt(MessageEntity::getId, beforeId);
        return list(w);
    }
}
