package com.exadel.frs.core.trainservice.repository.postgres;

import com.exadel.frs.core.trainservice.entity.postgres.Model;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional("tmPg")
public interface ModelRepositoryPg extends JpaRepository<Model, Long> {

    Optional<Model> findByApiKey(String apiKey);
}
