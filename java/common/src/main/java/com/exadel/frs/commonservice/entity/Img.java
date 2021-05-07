package com.exadel.frs.commonservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
public class Img {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "content")
    private byte[] content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToOne(mappedBy = "img", fetch = FetchType.LAZY)
    private Embedding embedding;
}