package com.exadel.frs.repository;

import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.AppModelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppModelRepository extends JpaRepository<AppModel, AppModelId> {

    Optional<AppModel> findByAppApiKeyAndModelApiKey(String appApiKey, String modelApiKey);

}
