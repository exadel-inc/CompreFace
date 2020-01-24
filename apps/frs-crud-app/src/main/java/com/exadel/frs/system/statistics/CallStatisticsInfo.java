package com.exadel.frs.system.statistics;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "system_call_statistics")
@Data
@EqualsAndHashCode(of = {"guid"})
public class CallStatisticsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "call_statistics_info_seq")
    @SequenceGenerator(name = "call_statistics_info_seq", sequenceName = "call_statistics_info_seq", allocationSize = 1)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;
    private String guid;
    private long callCount;
    private String objectGuid;
}
