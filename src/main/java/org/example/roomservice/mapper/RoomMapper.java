package org.example.roomservice.mapper;

import org.example.roomservice.dto.RoomDto;
import org.example.roomservice.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "direction.id", target = "directionId")
    RoomDto toDto(Room room);

    @Mapping(source = "directionId", target = "direction.id")
    Room toEntity(RoomDto dto);
}
