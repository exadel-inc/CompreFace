package com.exadel.frs.system.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

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
