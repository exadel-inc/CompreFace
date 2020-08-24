package com.exadel.frs.core.trainservice.entity;

import static javax.persistence.GenerationType.SEQUENCE;
import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trained_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"modelKey"})
public class TrainedModel {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "trained_model_id_seq")
    @SequenceGenerator(name = "trained_model_id_seq", sequenceName = "trained_model_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "model_key")
    private String modelKey;

    @Column(name = "classifier")
    private Classifier classifier;

    @Column(name = "calculator_version")
    private String calculatorVersion;
}
