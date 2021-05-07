package com.exadel.frs.commonservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "public")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Subject {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "subject_name")
    private String subjectName;
}
