package com.wait.server.controller;

import com.wait.server.dto.R;
import com.wait.server.dto.MatchCandidateVO;
import com.wait.server.security.AuthContext;
import com.wait.server.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final AuthContext authContext;
    private final MatchService matchService;

    @GetMapping("/candidates")
    public R<List<MatchCandidateVO>> candidates(
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) Double radiusKm) {
        return R.ok(matchService.candidates(authContext.currentUserId(), lng, lat, radiusKm));
    }
}
