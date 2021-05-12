package com.exadel.frs.commonservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@NamedEntityGraph(name = "img-with-subject", attributeNodes = @NamedAttributeNode("subject"))
@NamedEntityGraph(name = "img-with-embedding", attributeNodes = @NamedAttributeNode("embedding"))
public class Img {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "content")
    private byte[] content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "img")
    private Embedding embedding;
}