package com.exadel.frs.repository;

import com.exadel.frs.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppRepository extends JpaRepository<App, Long> {

    List<App> findAllByOrganizationId(Long organizationId);

    List<App> findAllByOrganizationIdAndUserAppRoles_Id_UserId(Long organizationId, Long userId);

}
