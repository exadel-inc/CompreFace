package com.exadel.frs.entity;

import com.exadel.frs.helpers.ModelAccessType;
import com.exadel.frs.helpers.ModelAccessTypeConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"app", "model"})
public class AppModel {

    @EmbeddedId
    private AppModelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("appId")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private App app;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("modelId")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Model model;

    @Convert(converter = ModelAccessTypeConverter.class)
    private ModelAccessType accessType;

}
