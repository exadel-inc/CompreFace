package com.exadel.frs.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"appId", "modelId"})
public class AppModelId implements Serializable {

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "model_id")
    private Long modelId;

}
