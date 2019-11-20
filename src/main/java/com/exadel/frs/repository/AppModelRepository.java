package com.exadel.frs.repository;

import com.exadel.frs.entity.AppModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppModelRepository extends JpaRepository<AppModel, Long> {

    Optional<AppModel> findByAppGuidAndModelGuid(String appGuid, String modelGuid);

}
