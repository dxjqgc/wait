package com.wait.server.controller;

import com.wait.server.dto.GreetDTO;
import com.wait.server.dto.R;
import com.wait.server.dto.TagRequirementViewVO;
import com.wait.server.entity.ConversationEntity;
import com.wait.server.security.AuthContext;
import com.wait.server.service.ConversationService;
import com.wait.server.service.TagRequirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class GreetController {

    private final AuthContext authContext;
    private final TagRequirementService tagRequirementService;
    private final ConversationService conversationService;

    /** 打招呼前：拉取对方的标签要求 + 自己是否已逐项确认 */
    @PostMapping("/{targetId}/pre-greet")
    public R<TagRequirementViewVO> preGreet(@PathVariable Long targetId) {
        Long me = authContext.currentUserId();
        return R.ok(tagRequirementService.view(me, targetId, "GREETING"));
    }

    @PostMapping("/{targetId}/greet")
    public R<Long> greet(@PathVariable Long targetId, @Valid @RequestBody GreetDTO dto) {
        Long me = authContext.currentUserId();
        ConversationEntity c = conversationService.greet(me, targetId, dto);
        return R.ok(c.getId());
    }
}
