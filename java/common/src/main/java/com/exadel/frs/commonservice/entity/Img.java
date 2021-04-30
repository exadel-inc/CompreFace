package com.exadel.frs.commonservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class Img {

    @Id // stub?
    private String id;

    @Column(name = "raw_img_fs")
    private byte[] rawImg;

    @Type(type = "jsonb")
    @Column(name = "embeddings")
    private Face.Embedding embedding;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Embedding {
        // list size: 512
        private List<Double> embeddings;
        private String calculatorVersion;
    }
}