package org.example.roomservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresenceDto {

    private Long user_id;

    private Boolean online;

}