package com.exadel.frs.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name="client")
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_id_seq")
    @SequenceGenerator(name = "client_id_seq", sequenceName = "client_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String email;
    private String password;

}
