package org.example.roomservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.dto.DirectionDto;
import org.example.roomservice.mapper.DirectionMapper;
import org.example.roomservice.model.Direction;
import org.example.roomservice.service.DirectionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
public class DirectionController {

    private final DirectionService directionService;
    private final DirectionMapper directionMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DirectionDto createDirection(@RequestBody DirectionDto dto) {
        Direction direction = directionService.createDirection(dto.getName(), dto.getSlug(), dto.getDescription());
        return directionMapper.toDto(direction);
    }

    @GetMapping
    public List<DirectionDto> getAllDirections() {
        return directionService.getAllDirections()
                .stream()
                .map(directionMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDirection(@PathVariable String slug) {
        directionService.deleteDirection(slug);
    }
}
