package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.TableLock;
import com.exadel.frs.commonservice.enums.TableName;
import java.util.UUID;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface TableLockRepository extends JpaRepository<TableLock, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TableLock findByTableName(TableName tableName);
}
