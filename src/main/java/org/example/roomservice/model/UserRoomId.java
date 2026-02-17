package org.example.roomservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoomId implements Serializable {

    private Long userId;
    private Long roomId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoomId)) return false;
        UserRoomId that = (UserRoomId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roomId);
    }
}
