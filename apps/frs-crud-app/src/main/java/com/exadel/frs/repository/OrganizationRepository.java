package com.exadel.frs.repository;

import com.exadel.frs.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByGuid(String guid);

    List<Organization> findAllByUserOrganizationRoles_Id_UserId(Long userId);

}
