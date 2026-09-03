package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private Integer age;

    private LocalDate birthday;

    /** 省/市/区县的行政区划代码（VARCHAR(6)） */
    private String provinceCode;

    private String cityCode;

    private String districtCode;

    private String profession;

    private Integer height;

    private String education;

    /** 性格特点，JSON 数组字符串；MyBatis-Plus 不直接映射 List，由 service 层处理 */
    private String personality;

    private String appearances;

    private String hobbies;

    private String tags;

    private String requirementPreset;

    private String requirementCustom;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
