package com.exadel.frs.commonservice.entity;

import com.vladmihalcea.hibernate.type.array.DoubleArrayType;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.UUID;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(schema = "public")
@Builder
@AllArgsConstructor
@TypeDefs(@TypeDef(name = "double-array", typeClass = DoubleArrayType.class))
@NamedEntityGraph(name = "embedding-with-subject", attributeNodes = {@NamedAttributeNode("subject"), @NamedAttributeNode("img")})
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
