package com.exadel.frs.repository;

import com.exadel.frs.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    Optional<Model> findByIdAndOwnerId(Long id, Long clientId);

    List<Model> findAllByOwnerId(Long clientId);

    void deleteByIdAndOwnerId(Long id, Long clientId);

}
