package com.exadel.frs.entity;

import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.helpers.ModelAccessTypeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"app", "model"})
public class AppModel {

    @EmbeddedId
    private AppModelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("appId")
    private App app;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("modelId")
    private Model model;

    @Convert(converter = ModelAccessTypeConverter.class)
    private AppModelAccess accessType;

    public AppModel(App app, Model model, AppModelAccess accessType) {
        this.app = app;
        this.model = model;
        this.accessType = accessType;
        this.id = new AppModelId(app.getId(), model.getId());
    }
}
