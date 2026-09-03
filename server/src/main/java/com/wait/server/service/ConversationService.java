package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wait.server.dto.GreetDTO;
import com.wait.server.entity.ConversationEntity;
import com.wait.server.entity.UserEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.mapper.ConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService extends ServiceImpl<ConversationMapper, ConversationEntity> {

    private final UserService userService;
    private final TagRequirementService tagRequirementService;

    /** 我正在 PENDING / ACTIVE 的会话中所涉及的"对方"用户 id 集合 */
    public Set<Long> engagedPartnerIds(Long meId) {
        LambdaQueryWrapper<ConversationEntity> w = new LambdaQueryWrapper<ConversationEntity>()
                .in(ConversationEntity::getState, "PENDING", "ACTIVE");
        // initiator=me → target 是对方
        // target=me → initiator 是对方
        java.util.List<ConversationEntity> mine = list(w);
        Set<Long> r = new java.util.HashSet<>();
        for (ConversationEntity c : mine) {
            if (c.getInitiatorId().equals(meId)) r.add(c.getTargetId());
            if (c.getTargetId().equals(meId)) r.add(c.getInitiatorId());
        }
        return r;
    }

    /** 发起招呼。规则：发起方须 FREE，目标方须 FREE；双方互相已确认对方的标签要求 */
    @Transactional
    public ConversationEntity greet(Long initiatorId, Long targetId, GreetDTO dto) {
        if (initiatorId.equals(targetId)) throw new BusinessException(400, "cannot greet self");

        UserEntity initiator = userService.getById(initiatorId);
        UserEntity target = userService.getById(targetId);
        if (initiator == null || target == null) throw new BusinessException(404, "user not found");
        if (!"FREE".equals(initiator.getEngagementState())) {
            throw new BusinessException(409, "you are ENGAGED, cannot start new greeting");
        }
        if (!"FREE".equals(target.getEngagementState())) {
            throw new BusinessException(409, "target is ENGAGED");
        }
        if (target.getMatchVisibility() == null || target.getMatchVisibility() != 1) {
            throw new BusinessException(409, "target not visible");
        }
        // 不能重复对同一目标 PENDING
        long exists = count(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getInitiatorId, initiatorId)
                .eq(ConversationEntity::getTargetId, targetId)
                .eq(ConversationEntity::getState, "PENDING"));
        if (exists > 0) throw new BusinessException(409, "you already have a pending greeting to this user");

        // 双向标签要求确认
        boolean initiatorConfirmedTarget = tagRequirementService.allConfirmed(initiatorId, targetId, "GREETING");
        if (!initiatorConfirmedTarget) {
            throw new BusinessException(409, "please confirm target's tag requirements first");
        }
        // 写确认记录（确保）
        if (dto.getConfirmations() != null) {
            for (GreetDTO.ConfirmItem ci : dto.getConfirmations()) {
                tagRequirementService.recordConfirmation(initiatorId, targetId, "GREETING",
                        ci.getKey(), ci.getValue());
            }
        }

        // 创建 PENDING 会话，发起方仍 FREE
        ConversationEntity c = new ConversationEntity();
        c.setInitiatorId(initiatorId);
        c.setTargetId(targetId);
        c.setGreetingMsg(dto.getGreeting());
        c.setState("PENDING");
        c.setInitiatorReadAt(LocalDateTime.now());
        save(c);
        return c;
    }

    /** 接收方首次回复：PENDING → ACTIVE；接收方 ENGAGED；同时把接收方其它 PENDING 自动 ENDED */
    @Transactional
    public ConversationEntity replyActivate(Long conversationId, Long replierId) {
        ConversationEntity c = getById(conversationId);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!"PENDING".equals(c.getState())) {
            throw new BusinessException(409, "conversation is not PENDING");
        }
        Long replierUserId;
        if (c.getInitiatorId().equals(replierId)) {
            // 发起方主动续聊：不算激活；此方法只处理接收方首次回复
            throw new BusinessException(400, "use message endpoint to continue an active conversation");
        }
        if (!c.getTargetId().equals(replierId)) {
            throw new BusinessException(403, "not a participant");
        }

        c.setState("ACTIVE");
        c.setTargetReadAt(LocalDateTime.now());
        c.setLastMsgAt(LocalDateTime.now());
        updateById(c);

        // 接收方进入 ENGAGED（首次回复时）
        UserEntity target = userService.getById(replierId);
        if ("FREE".equals(target.getEngagementState())) {
            target.setEngagementState("ENGAGED");
            userService.updateById(target);
        }
        // 发起方进入 ENGAGED
        UserEntity initiator = userService.getById(c.getInitiatorId());
        if ("FREE".equals(initiator.getEngagementState())) {
            initiator.setEngagementState("ENGAGED");
            userService.updateById(initiator);
        }
        // 接收方其它 PENDING 会话自动 ENDED（被回复的保留）
        endOtherPendingForTarget(replierId, conversationId);
        // 发起方其它 PENDING 会话也自动 ENDED（已占用）
        endOtherPendingForInitiator(c.getInitiatorId(), conversationId);

        return c;
    }

    private void endOtherPendingForTarget(Long targetId, Long keepConvId) {
        java.util.List<ConversationEntity> others = list(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getTargetId, targetId)
                .eq(ConversationEntity::getState, "PENDING")
                .ne(ConversationEntity::getId, keepConvId));
        for (ConversationEntity o : others) {
            o.setState("ENDED");
            o.setEndedReason("TIMEOUT_UNREPLIED"); // 语义上：对方先回复了别人
            o.setEndedBy(targetId);
            updateById(o);
        }
    }

    private void endOtherPendingForInitiator(Long initiatorId, Long keepConvId) {
        java.util.List<ConversationEntity> others = list(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getInitiatorId, initiatorId)
                .eq(ConversationEntity::getState, "PENDING")
                .ne(ConversationEntity::getId, keepConvId));
        for (ConversationEntity o : others) {
            o.setState("ENDED");
            o.setEndedReason("MANUAL");
            o.setEndedBy(initiatorId);
            updateById(o);
        }
    }

    /** 主动结束会话：state → ENDED，双方回 FREE */
    @Transactional
    public ConversationEntity end(Long conversationId, Long userId) {
        ConversationEntity c = getById(conversationId);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if ("ENDED".equals(c.getState())) return c;
        if (!c.getInitiatorId().equals(userId) && !c.getTargetId().equals(userId)) {
            throw new BusinessException(403, "not a participant");
        }
        if ("PENDING".equals(c.getState()) && !c.getInitiatorId().equals(userId)) {
            throw new BusinessException(400, "target cannot end a PENDING greeting; just ignore it");
        }
        c.setState("ENDED");
        c.setEndedBy(userId);
        c.setEndedReason("MANUAL");
        updateById(c);

        // 双方回 FREE
        freeIfEngaged(c.getInitiatorId());
        freeIfEngaged(c.getTargetId());
        return c;
    }

    private void freeIfEngaged(Long userId) {
        UserEntity u = userService.getById(userId);
        if (u != null && "ENGAGED".equals(u.getEngagementState())) {
            // 仍可能有别的 ACTIVE？理论上不应有，但兜底
            long active = count(new LambdaQueryWrapper<ConversationEntity>()
                    .eq(ConversationEntity::getState, "ACTIVE")
                    .and(w -> w.eq(ConversationEntity::getInitiatorId, userId)
                            .or().eq(ConversationEntity::getTargetId, userId)));
            if (active == 0) {
                u.setEngagementState("FREE");
                userService.updateById(u);
            }
        }
    }

    public void markRead(Long conversationId, Long userId) {
        ConversationEntity c = getById(conversationId);
        if (c == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (c.getInitiatorId().equals(userId) && c.getInitiatorReadAt() == null) {
            c.setInitiatorReadAt(now);
            updateById(c);
        } else if (c.getTargetId().equals(userId) && c.getTargetReadAt() == null) {
            c.setTargetReadAt(now);
            updateById(c);
        }
    }
}
