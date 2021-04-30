package com.exadel.frs.commonservice.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@EqualsAndHashCode(of = "id")
public class Subject {

    @Id
    private String id;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "subject_name")
    private String subjectName;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private Collection<Img> images;
}
