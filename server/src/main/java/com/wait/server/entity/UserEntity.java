package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Casdoor 用户唯一标识 */
    private String casdoorId;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 0-未激活 1-正常 2-封禁 */
    private Integer status;

    /** 会话占用状态：FREE-自由 ENGAGED-占用中 */
    private String engagementState;

    private LocalDateTime lastActiveAt;

    private BigDecimal lastLng;

    private BigDecimal lastLat;

    private LocalDateTime locatedAt;

    /** 0-不可被匹配 1-可被匹配 */
    private Integer matchVisibility;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
