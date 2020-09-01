package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.entity.TrainedModel;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface TrainedModelRepository extends JpaRepository<TrainedModel, Long> {

    List<TrainedModel> findByClassifierIsNull();

    Optional<TrainedModel> findFirstByModelKey(String modelKey);

    void deleteByModelKey(String modelKey);
}
