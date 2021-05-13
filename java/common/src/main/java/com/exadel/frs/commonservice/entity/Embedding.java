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
@NamedEntityGraph(name = "embedding-with-subject", attributeNodes = @NamedAttributeNode("subject"))
public class Embedding {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Type(type = "double-array")
    @Column(
            name = "embedding",
            columnDefinition = "float8[]",
            nullable = false
    )
    private double[] embedding;

    @Column(nullable = false)
    private String calculator;

    // Optional.
    // There could be predefined embeddings without image (pre-inserted demo embeddings).
    // There could multiple embeddings for same image (calculated with diff calculators).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_id", referencedColumnName = "id")
    private Img img;
}
