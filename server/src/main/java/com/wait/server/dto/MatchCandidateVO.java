package com.wait.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MatchCandidateVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer age;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;
    private String profession;
    private Integer height;
    private String education;
    private List<String> personality;
    private List<String> appearances;
    private List<String> hobbies;
    private List<String> tags;
    /** 距离 km，可能为 null */
    private Double distanceKm;
    /** 标签匹配度 0-100 */
    private Integer matchScore;
    /** 对方对我的标签要求（用于打招呼前展示） */
    private Map<String, Object> requirementPreset;
    private List<Map<String, String>> requirementCustom;
    private Boolean requirementMatched;
}
