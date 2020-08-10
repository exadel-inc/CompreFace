package com.exadel.frs.core.trainservice.entity.postgres;

import static javax.persistence.GenerationType.SEQUENCE;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
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
    @Transient
    private Long id;
    @Column(name = "face_name")
    private String faceName;
    @Column(name = "api_key")
    private String apiKey;
    private String guid;
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
