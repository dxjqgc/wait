package com.wait.server.dto;

import lombok.Data;

@Data
public class RegionVO {
    private String code;
    private String name;
    private Integer level;
    private String parentCode;
}
