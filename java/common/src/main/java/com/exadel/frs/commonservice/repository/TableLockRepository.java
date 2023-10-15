package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.TableLock;
import com.exadel.frs.commonservice.enums.TableLockName;
import java.util.UUID;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TableLockRepository extends JpaRepository<TableLock, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tl from TableLock tl where UPPER(tl.lockName) = UPPER(:#{#lockName?.toString()})")
    TableLock lockByName(@Param("lockName") TableLockName lockName);
}
