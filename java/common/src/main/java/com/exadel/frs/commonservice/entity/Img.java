package com.exadel.frs.commonservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.UUID;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(schema = "public")
public class Img {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "content")
    private byte[] content;
}
