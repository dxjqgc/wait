package com.wait.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private Integer status;
    private String engagementState;
    private Integer matchVisibility;
    private LocalDateTime createdAt;
}
