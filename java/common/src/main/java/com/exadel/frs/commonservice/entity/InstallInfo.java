package com.exadel.frs.commonservice.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "install_info", schema = "public")
public class InstallInfo {

    @Id
    @Column(name = "install_guid")
    private String installGuid;
}
