package com.exadel.frs.repository;

import com.exadel.frs.entity.ModelShareRequest;
import com.exadel.frs.entity.ModelShareRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ModelShareRequestRepository extends JpaRepository<ModelShareRequest, ModelShareRequestId> {

    @Query("select m from ModelShareRequest m where m.id.requestId = :requestId")
    ModelShareRequest findModelShareRequestByRequestId(UUID requestId);
}