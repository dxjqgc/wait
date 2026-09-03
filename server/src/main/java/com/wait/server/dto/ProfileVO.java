package com.wait.server.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ProfileVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String realName;
    private Integer age;
    private LocalDate birthday;
    private String city;
    private String district;
    private String profession;
    private Integer height;
    private String education;
    private List<String> personality;
    private List<String> appearances;
    private List<String> hobbies;
    private List<String> tags;
    /** 对方预设要求：{heightMin, heightMax, ageMin, ageMax, education, ...} */
    private Map<String, Object> requirementPreset;
    /** 对方自定义要求：[{key, value}] */
    private List<Map<String, String>> requirementCustom;
    private Boolean matchVisibility;
}
