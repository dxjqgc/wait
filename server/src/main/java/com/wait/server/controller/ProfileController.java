package com.wait.server.controller;

import com.wait.server.dto.R;
import com.wait.server.dto.UpdateProfileDTO;
import com.wait.server.dto.ProfileVO;
import com.wait.server.entity.UserEntity;
import com.wait.server.entity.UserProfileEntity;
import com.wait.server.security.AuthContext;
import com.wait.server.service.UserProfileService;
import com.wait.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthContext authContext;
    private final UserService userService;
    private final UserProfileService userProfileService;

    @GetMapping
    public R<ProfileVO> getMyProfile() {
        Long userId = authContext.currentUserId();
        UserEntity user = userService.getById(userId);
        UserProfileEntity p = userProfileService.findOrCreateByUserId(userId);
        return R.ok(userProfileService.toVO(user, p));
    }

    @PutMapping
    public R<ProfileVO> updateMyProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = authContext.currentUserId();
        return R.ok(userProfileService.updateProfile(userId, dto));
    }
}
