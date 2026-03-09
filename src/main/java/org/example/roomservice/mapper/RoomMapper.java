package org.example.roomservice.mapper;

import org.example.roomservice.dto.RoomListDto;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "direction.id", target = "directionId")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "tagsToNames")
    RoomListDto toDto(Room room);

    @Mapping(source = "directionId", target = "direction.id")
    @Mapping(target = "tags", ignore = true)
    Room toEntity(RoomListDto dto);

    @Named("tagsToNames")
    default Set<String> tagsToNames(Set<Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
}
