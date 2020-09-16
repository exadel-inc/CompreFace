package com.exadel.frs.core.trainservice.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class Face {

    @Id
    private String id;
    @Column(name = "face_name")
    private String faceName;
    @Column(name = "api_key")
    private String apiKey;
    @Type(type = "json")
    @Column(name = "embeddings")
    private Embedding embedding;
    @Column(name = "raw_img_fs")
    private byte[] rawImg;
    @Column(name = "face_img_fs")
    private byte[] faceImg;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Embedding implements Serializable {

        private List<Double> embeddings;
        private String calculatorVersion;
    }
}
