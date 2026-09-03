package com.wait.server.controller;

import com.wait.server.dto.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public R<Map<String, Object>> health() {
        return R.ok(Map.of(
                "status", "UP",
                "service", "wait-server"
        ));
    }
}
