package org.example.roomservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceRequest {

    private List<Long> user_ids;

}