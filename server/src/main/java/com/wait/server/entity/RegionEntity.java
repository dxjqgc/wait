package com.wait.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("region")
public class RegionEntity {

    @TableId(type = IdType.INPUT)
    private String code;

    private String name;

    /** 1-省 2-市 3-区县 */
    private Integer level;

    private String parentCode;
}
