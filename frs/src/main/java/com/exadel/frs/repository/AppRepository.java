package com.exadel.frs.repository;

import com.exadel.frs.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppRepository extends JpaRepository<App, Long> {

    Optional<App> findByIdAndOwnerId(Long id, Long clientId);

    List<App> findAllByOwnerId(Long clientId);

    void deleteByIdAndOwnerId(Long id, Long clientId);

}
