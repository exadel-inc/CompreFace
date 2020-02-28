package com.exadel.frs.repository;

import com.exadel.frs.entity.ModelShareRequest;
import com.exadel.frs.entity.ModelShareRequestId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelShareRequestRepository extends JpaRepository<ModelShareRequest, ModelShareRequestId> {
}