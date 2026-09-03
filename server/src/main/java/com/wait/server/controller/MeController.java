package com.wait.server.controller;

import com.wait.server.dto.R;
import com.wait.server.dto.UserVO;
import com.wait.server.entity.UserEntity;
import com.wait.server.security.AuthContext;
import com.wait.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthContext authContext;
    private final UserService userService;

    @GetMapping
    public R<UserVO> me() {
        String casdoorId = authContext.currentCasdoorId();
        UserEntity user = userService.findByCasdoorId(casdoorId);
        if (user == null) {
            // AuthContext.currentUserId() 在 user 未绑定时会抛错；
            // 这里手动建影子用户，仅当 casdoorId 不在库时
            user = new UserEntity();
            user.setCasdoorId(casdoorId);
            user.setUsername(casdoorId);
            user.setNickname(casdoorId);
            user.setGender(0);
            user.setStatus(1);
            user.setEngagementState("FREE");
            user.setMatchVisibility(1);
            user.setLastActiveAt(LocalDateTime.now());
            userService.save(user);
        } else {
            user.setLastActiveAt(LocalDateTime.now());
            userService.updateById(user);
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return R.ok(vo);
    }
}
