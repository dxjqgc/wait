package com.wait.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class GreetDTO {
    @NotBlank
    @Size(max = 256)
    private String greeting;

    /** 标签要求确认项（key/value 来自 pre-greet 返回） */
    private List<ConfirmItem> confirmations;

    @Data
    public static class ConfirmItem {
        @NotNull
        private String key;
        private String value;
        @NotNull
        private Boolean confirmed;
    }
}
