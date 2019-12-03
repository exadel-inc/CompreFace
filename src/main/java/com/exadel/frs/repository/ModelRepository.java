package com.exadel.frs.repository;

import com.exadel.frs.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    List<Model> findAllByAppModelAccess_Id_AppId(Long appId);

    Optional<Model> findByGuid(String guid);

}
