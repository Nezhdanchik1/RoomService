package org.example.roomservice.mapper;

import org.example.roomservice.dto.UserRoomDto;
import org.example.roomservice.model.UserRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRoomMapper {
    UserRoomDto toDto(UserRoom userRoom);
}
