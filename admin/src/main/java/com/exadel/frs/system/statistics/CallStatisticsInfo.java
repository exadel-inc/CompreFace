/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
