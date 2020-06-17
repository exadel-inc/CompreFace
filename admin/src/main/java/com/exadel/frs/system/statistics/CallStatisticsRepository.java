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

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CallStatisticsRepository extends JpaRepository<CallStatisticsInfo, Long> {
    @Transactional
    @Modifying
    @Query(value = "insert into system_call_statistics(id,object_type,guid,object_guid)" +
            " values(nextval('call_statistics_info_seq'),:type,:guid,:oGuid)" +
            " on conflict on constraint call_statistics_object_index" +
            " do update set call_count=system_call_statistics.call_count+1", nativeQuery = true)
    void updateCallStatistics(@Param("type") String type, @Param("guid") String guid, @Param("oGuid") String objectGuid);
}
