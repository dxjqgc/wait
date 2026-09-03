package com.wait.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDTO {
    @NotBlank
    @Size(max = 2048)
    private String content;
}
