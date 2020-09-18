package com.exadel.frs.core.trainservice.cache;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CachedFace {

    private String name;
    private String imageId;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CachedFace face = (CachedFace) o;
        System.out.println("imageId 1 = " + imageId);
        System.out.println("imageId 2 = " + face.imageId);
        return Objects.equal(imageId, face.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(imageId);
    }
}
