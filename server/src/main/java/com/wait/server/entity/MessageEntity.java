package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class MessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long senderId;

    private String content;

    private LocalDateTime createdAt;
}
