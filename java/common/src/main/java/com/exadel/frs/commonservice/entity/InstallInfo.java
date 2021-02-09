package com.exadel.frs.commonservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "install_info")
public class InstallInfo {

    @Id
    private String installGuid;
}