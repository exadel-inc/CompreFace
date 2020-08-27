package com.exadel.frs.core.trainservice.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
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
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
public class Face {

    @Id
    private String id;
    @Column(name = "face_name")
    private String faceName;
    @Column(name = "api_key")
    private String apiKey;
    @Type(type = "jsonb")
    @Column(name = "embeddings")
    private Embedding embedding;
    @Column(name = "raw_img_fs")
    private byte[] rawImg;
    @Column(name = "face_img_fs")
    private byte[] faceImg;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Embedding {

        private List<Double> embeddings;
        private String calculatorVersion;
    }
}
