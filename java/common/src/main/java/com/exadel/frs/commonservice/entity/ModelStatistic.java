package com.exadel.frs.commonservice.entity;

import static javax.persistence.GenerationType.SEQUENCE;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "model_statistic", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelStatistic {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "model_statistic_id_seq")
    @SequenceGenerator(name = "model_statistic_id_seq", sequenceName = "model_statistic_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "request_count")
    private Integer requestCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", referencedColumnName = "id")
    private Model model;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
