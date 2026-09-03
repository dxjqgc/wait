package com.wait.server.security;

import com.wait.server.entity.UserEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthContext {

    private final UserService userService;

    public Jwt currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException(401, "not authenticated");
        }
        return jwt;
    }

    public String currentCasdoorId() {
        Jwt jwt = currentJwt();
        String name = jwt.getClaimAsString("name");
        return name != null && !name.isBlank() ? name : jwt.getSubject();
    }

    public Long currentUserId() {
        String casdoorId = currentCasdoorId();
        UserEntity user = userService.findByCasdoorId(casdoorId);
        if (user == null) {
            throw new BusinessException(404, "local user not bound, call /api/me first");
        }
        return user.getId();
    }
}
