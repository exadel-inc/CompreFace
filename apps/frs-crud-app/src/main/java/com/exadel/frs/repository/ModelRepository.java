package com.exadel.frs.repository;

import com.exadel.frs.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @Query("select distinct m " +
            "from Model m " +
            "left join AppModel am on m.id = am.id.modelId " +
            "where am.id.appId = :appId OR m.app.id = :appId")
    List<Model> findAllByAppId(Long appId);

    Optional<Model> findByGuid(String guid);

    boolean existsByNameAndAppId(String name, Long appId);

}
