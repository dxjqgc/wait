package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class ConversationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long initiatorId;

    private Long targetId;

    private String greetingMsg;

    /** PENDING / ACTIVE / ENDED */
    private String state;

    private Long endedBy;

    /** MANUAL / TIMEOUT_UNREAD / TIMEOUT_UNREPLIED */
    private String endedReason;

    private LocalDateTime initiatorReadAt;

    private LocalDateTime targetReadAt;

    private LocalDateTime lastMsgAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
