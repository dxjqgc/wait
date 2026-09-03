package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wait.server.entity.UserEntity;
import com.wait.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, UserEntity> {

    public UserEntity findByCasdoorId(String casdoorId) {
        return getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getCasdoorId, casdoorId)
                .last("LIMIT 1"));
    }

    public UserEntity findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .last("LIMIT 1"));
    }
}
