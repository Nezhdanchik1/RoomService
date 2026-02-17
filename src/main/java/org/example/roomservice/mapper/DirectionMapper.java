package org.example.roomservice.mapper;

import org.example.roomservice.dto.DirectionDto;
import org.example.roomservice.model.Direction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DirectionMapper {
    DirectionDto toDto(Direction direction);
    Direction toEntity(DirectionDto dto);
}
