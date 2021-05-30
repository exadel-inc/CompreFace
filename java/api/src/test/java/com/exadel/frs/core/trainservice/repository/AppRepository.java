package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.commonservice.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// App repository for test purposes
@Repository
public interface AppRepository extends JpaRepository<App, Long> {
    // add jpa test helper methods here
}
