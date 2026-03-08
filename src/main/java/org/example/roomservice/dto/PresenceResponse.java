package org.example.roomservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceResponse {

    private Integer online_count;

    private List<UserPresenceDto> users;

}