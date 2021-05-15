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
// Think about Subject as a collection of Embeddings
public class Subject {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "api_key")
    private String apiKey;

    // String that identifies subject in unique way
    // Could be guid, UUID, user first + last name - up to cutomers
    @Column(name = "subject_name")
    private String subjectName;
}
