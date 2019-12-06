package com.exadel.frs.repository;

import com.exadel.frs.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findAllByUserOrganizationRoles_Id_UserId(Long userId);

}
