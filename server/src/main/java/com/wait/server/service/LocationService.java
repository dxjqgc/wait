package com.wait.server.service;

import com.wait.server.entity.UserEntity;
import com.wait.server.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserService userService;

    public void updateLocation(Long userId, BigDecimal lng, BigDecimal lat) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new com.wait.server.exception.BusinessException(404, "user not found");
        }
        user.setLastLng(lng);
        user.setLastLat(lat);
        user.setLocatedAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        userService.updateById(user);
    }
}
