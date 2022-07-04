package com.exadel.frs.commonservice.entity;

import com.exadel.frs.commonservice.enums.TableLockName;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_lock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableLock {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_name")
    private TableLockName lockName;
}
