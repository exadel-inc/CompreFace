package com.exadel.frs.commonservice.entity;

import com.vladmihalcea.hibernate.type.array.DoubleArrayType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@TypeDefs(@TypeDef(name = "double-array", typeClass = DoubleArrayType.class))
public class Embedding {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Type(type = "double-array")
    @Column(
            name = "embedding",
            columnDefinition = "float8[]",
            nullable = false
    )
    private Double[] embedding;

    @Column(nullable = false)
    private String calculator;

    // Optional. There could be predefined embedding without img as well as img without embedding.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_id", referencedColumnName = "id")
    private Img img;
}
