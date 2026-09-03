package com.wait.server.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wait.server.entity.ConversationEntity;
import com.wait.server.entity.UserEntity;
import com.wait.server.service.ConversationService;
import com.wait.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationTimeoutJob {

    private final ConversationService conversationService;
    private final UserService userService;

    /** 每 5 分钟扫一次超时会话 */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void scanTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pendingUnreadCutoff = now.minusHours(6);
        LocalDateTime pendingReadUnrepliedCutoff = now.minusHours(2);

        List<ConversationEntity> pendings = conversationService.list(
                new LambdaQueryWrapper<ConversationEntity>()
                        .eq(ConversationEntity::getState, "PENDING"));
        if (pendings.isEmpty()) return;

        Set<Long> freedUserIds = new HashSet<>();
        for (ConversationEntity c : pendings) {
            // 6h 接收方未读
            if (c.getTargetReadAt() == null && c.getCreatedAt().isBefore(pendingUnreadCutoff)) {
                endTimeout(c, "TIMEOUT_UNREAD", now);
                freedUserIds.add(c.getInitiatorId());
                continue;
            }
            // 2h 接收方已读未回
            if (c.getTargetReadAt() != null && c.getTargetReadAt().isBefore(pendingReadUnrepliedCutoff)
                    && (c.getLastMsgAt() == null || c.getLastMsgAt().isBefore(pendingReadUnrepliedCutoff))) {
                endTimeout(c, "TIMEOUT_UNREPLIED", now);
                freedUserIds.add(c.getInitiatorId());
            }
        }

        // 发起方在 PENDING 阶段仍是 FREE，所以无需解放；
        // 但若该发起方有别的 ACTIVE，此处不处理
        log.debug("timeout scan finished, ended {} conversations", freedUserIds.size());
    }

    private void endTimeout(ConversationEntity c, String reason, LocalDateTime now) {
        c.setState("ENDED");
        c.setEndedReason(reason);
        c.setEndedBy(c.getTargetId());
        conversationService.updateById(c);
    }
}
