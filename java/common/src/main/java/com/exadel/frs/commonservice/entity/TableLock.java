package com.exadel.frs.commonservice.entity;

import com.exadel.frs.commonservice.enums.TableName;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
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
    @GeneratedValue
    private UUID id;

    @Column(name = "table_name")
    @Enumerated(EnumType.STRING)
    private TableName tableName;
}
