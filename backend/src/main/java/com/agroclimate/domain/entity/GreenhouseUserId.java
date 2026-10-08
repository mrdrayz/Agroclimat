package com.agroclimate.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GreenhouseUserId implements Serializable {

    private Long greenhouseId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GreenhouseUserId that)) return false;
        return Objects.equals(greenhouseId, that.greenhouseId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(greenhouseId, userId);
    }
}
