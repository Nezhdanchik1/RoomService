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
public class UserDirectionId implements Serializable {

    private Long userId;
    private Long directionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDirectionId)) return false;
        UserDirectionId that = (UserDirectionId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(directionId, that.directionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, directionId);
    }
}
