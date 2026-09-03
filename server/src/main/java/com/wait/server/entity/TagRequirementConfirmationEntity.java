package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag_requirement_confirmation")
public class TagRequirementConfirmationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long confirmerId;

    private Long requirementOwnerId;

    private String requirementKey;

    private String requirementValue;

    /** GREETING / REPLY */
    private String direction;

    private Integer confirmed;

    private LocalDateTime createdAt;
}
