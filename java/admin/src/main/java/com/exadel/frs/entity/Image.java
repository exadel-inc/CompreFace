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

package com.exadel.frs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class Image {

    public Image(Image image) {
        this.id = image.id;
        this.rawImg = image.rawImg;
        this.faceImg = image.faceImg;
        this.face = image.face;
    }

    @Id
    private String id;

    @Column(name = "raw_img_fs")
    private byte[] rawImg;

    @Column(name = "face_img_fs")
    private byte[] faceImg;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Face face;
}
