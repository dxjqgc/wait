package com.wait.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wait.server.dto.*;
import com.wait.server.entity.ConversationEntity;
import com.wait.server.entity.MessageEntity;
import com.wait.server.entity.UserEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.security.AuthContext;
import com.wait.server.service.ConversationService;
import com.wait.server.service.MessageService;
import com.wait.server.service.TagRequirementService;
import com.wait.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final AuthContext authContext;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;
    private final TagRequirementService tagRequirementService;

    @GetMapping
    public R<List<ConversationVO>> listMine() {
        Long me = authContext.currentUserId();
        List<ConversationEntity> list = conversationService.list(new LambdaQueryWrapper<ConversationEntity>()
                .and(w -> w.eq(ConversationEntity::getInitiatorId, me)
                        .or().eq(ConversationEntity::getTargetId, me))
                .in(ConversationEntity::getState, "PENDING", "ACTIVE")
                .orderByDesc(ConversationEntity::getUpdatedAt));

        Map<Long, UserEntity> userCache = new HashMap<>();
        List<ConversationVO> vos = list.stream()
                .map(c -> toVO(c, me, userCache))
                .sorted(Comparator.comparing(ConversationVO::getLastMsgAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return R.ok(vos);
    }

    @GetMapping("/{id}")
    public R<ConversationVO> get(@PathVariable Long id) {
        Long me = authContext.currentUserId();
        ConversationEntity c = conversationService.getById(id);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!c.getInitiatorId().equals(me) && !c.getTargetId().equals(me)) {
            throw new BusinessException(403, "not a participant");
        }
        return R.ok(toVO(c, me, new HashMap<>()));
    }

    @PostMapping("/{id}/end")
    public R<Void> end(@PathVariable Long id) {
        Long me = authContext.currentUserId();
        conversationService.end(id, me);
        return R.ok();
    }

    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        Long me = authContext.currentUserId();
        conversationService.markRead(id, me);
        return R.ok();
    }

    @PostMapping("/{id}/pre-reply")
    public R<TagRequirementViewVO> preReply(@PathVariable Long id) {
        Long me = authContext.currentUserId();
        ConversationEntity c = conversationService.getById(id);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!c.getTargetId().equals(me)) {
            throw new BusinessException(403, "only target can reply to a greeting");
        }
        if (!"PENDING".equals(c.getState())) {
            throw new BusinessException(409, "conversation is not PENDING");
        }
        return R.ok(tagRequirementService.view(me, c.getInitiatorId(), "REPLY"));
    }

    @PostMapping("/{id}/reply")
    public R<Long> reply(@PathVariable Long id, @Valid @RequestBody SendMessageDTO dto) {
        Long me = authContext.currentUserId();
        ConversationEntity c = conversationService.getById(id);
        if (c == null) throw new BusinessException(404, "conversation not found");
        if (!c.getTargetId().equals(me)) throw new BusinessException(403, "only target can reply");

        // 接收方回复前必须确认过对方的标签要求（REPLY 阶段）
        boolean ok = tagRequirementService.allConfirmed(me, c.getInitiatorId(), "REPLY");
        if (!ok) throw new BusinessException(409, "please confirm initiator's tag requirements first");

        // 首次回复：激活会话 + 状态机切换
        if ("PENDING".equals(c.getState())) {
            conversationService.replyActivate(id, me);
        }
        MessageEntity m = messageService.send(id, me, dto.getContent());
        return R.ok(m.getId());
    }

    @PostMapping("/{id}/messages")
    public R<Long> send(@PathVariable Long id, @Valid @RequestBody SendMessageDTO dto) {
        Long me = authContext.currentUserId();
        MessageEntity m = messageService.send(id, me, dto.getContent());
        return R.ok(m.getId());
    }

    @GetMapping("/{id}/messages")
    public R<List<MessageVO>> listMessages(@PathVariable Long id,
                                           @RequestParam(required = false) Long beforeId,
                                           @RequestParam(defaultValue = "50") int limit) {
        Long me = authContext.currentUserId();
        List<MessageEntity> list = messageService.list(id, me, beforeId, limit);
        // 标记已读
        conversationService.markRead(id, me);
        return R.ok(list.stream().map(m -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).toList());
    }

    private ConversationVO toVO(ConversationEntity c, Long me, Map<Long, UserEntity> cache) {
        ConversationVO vo = new ConversationVO();
        BeanUtils.copyProperties(c, vo);
        UserEntity initiator = cache.computeIfAbsent(c.getInitiatorId(), userService::getById);
        UserEntity target = cache.computeIfAbsent(c.getTargetId(), userService::getById);
        if (initiator != null) {
            vo.setInitiatorNickname(initiator.getNickname());
            vo.setInitiatorAvatar(initiator.getAvatar());
        }
        if (target != null) {
            vo.setTargetNickname(target.getNickname());
            vo.setTargetAvatar(target.getAvatar());
        }
        vo.setIAmInitiator(c.getInitiatorId().equals(me));
        vo.setIAmTargetPendingReply(c.getTargetId().equals(me) && "PENDING".equals(c.getState()));
        return vo;
    }
}
