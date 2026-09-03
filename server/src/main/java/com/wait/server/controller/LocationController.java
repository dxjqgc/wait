package com.wait.server.controller;

import com.wait.server.dto.R;
import com.wait.server.dto.UpdateLocationDTO;
import com.wait.server.security.AuthContext;
import com.wait.server.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/location")
@RequiredArgsConstructor
public class LocationController {

    private final AuthContext authContext;
    private final LocationService locationService;

    @PostMapping
    public R<Void> update(@Valid @RequestBody UpdateLocationDTO dto) {
        locationService.updateLocation(authContext.currentUserId(), dto.getLng(), dto.getLat());
        return R.ok();
    }
}
