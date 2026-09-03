package com.wait.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class UpdateProfileDTO {

    private String nickname;
    private String avatar;
    private Integer gender;

    private String realName;
    private Integer age;
    private LocalDate birthday;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String profession;
    private Integer height;
    private String education;

    private List<String> personality;
    private List<String> appearances;
    private List<String> hobbies;
    private List<String> tags;

    private Map<String, Object> requirementPreset;
    private List<Map<String, String>> requirementCustom;

    private Boolean matchVisibility;

    @Min(0) @Max(1)
    private Integer matchVisibilityValue;
}
