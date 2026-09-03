package com.wait.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TagRequirementViewVO {
    private Long ownerId;
    private Map<String, Object> preset;
    private List<Map<String, String>> custom;
    /** 当前用户是否已逐项确认满足 */
    private List<ConfirmationItem> items;

    @Data
    public static class ConfirmationItem {
        private String key;
        private String value;
        private Boolean satisfied;
        private Boolean confirmed;
    }
}
