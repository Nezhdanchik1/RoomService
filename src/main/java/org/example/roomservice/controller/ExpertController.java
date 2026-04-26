package org.example.roomservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.model.UserDirection;
import org.example.roomservice.service.UserDirectionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experts")
@RequiredArgsConstructor
public class ExpertController {

    private final UserDirectionService userDirectionService;

    @PostMapping("/{directionSlug}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void assignExpert(@PathVariable String directionSlug, @PathVariable Long userId) {
        userDirectionService.assignExpert(userId, directionSlug);
    }

    @DeleteMapping("/{directionSlug}/remove/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeExpert(@PathVariable String directionSlug, @PathVariable Long userId) {
        userDirectionService.removeExpert(userId, directionSlug);
    }

    @GetMapping("/{directionSlug}")
    public List<Long> getExperts(@PathVariable String directionSlug) {
        return userDirectionService.getExpertsByDirection(directionSlug)
                .stream()
                .map(ud -> ud.getId().getUserId())
                .toList();
    }
}
