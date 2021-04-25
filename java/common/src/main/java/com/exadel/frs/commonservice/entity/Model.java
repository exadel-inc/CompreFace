/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.commonservice.entity;

import com.exadel.frs.commonservice.enums.AppModelAccess;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.helpers.ModelTypeConverter;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"guid"})
public class Model {

    public Model(Model model) {
        this.id = model.id;
        this.name = model.name;
        this.guid = randomUUID().toString();
        this.apiKey = randomUUID().toString();
        this.app = model.getApp();
        this.appModelAccess = model.appModelAccess;
        this.type = model.type;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;
    @Column(name = "api_key")
    private String apiKey;
    @Convert(converter = ModelTypeConverter.class)
    private ModelType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private App app;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppModel> appModelAccess = new ArrayList<>();

    public void addAppModelAccess(App app, AppModelAccess access) {
        AppModel appModel = new AppModel(app, this, access);
        appModelAccess.add(appModel);
        app.getAppModelAccess().add(appModel);
    }

    public Optional<AppModel> getAppModel(String appGuid) {
        return appModelAccess.stream()
                             .filter(appModel -> appModel.getApp().getGuid().equals(appGuid))
                             .findFirst();
    }
}