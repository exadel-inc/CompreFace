package com.exadel.frs.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "install_info")
public class InstallInfo {

    @Id
    private String installGuid;
}