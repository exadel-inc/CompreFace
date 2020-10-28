/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs;

import static com.exadel.frs.utils.TestUtils.executeScript;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@DisplayName("Test organization delete")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Nested
public class OrganizationServiceTestIT {

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelShareRequestRepository modelShareRequestRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void initDB() {
        log.info("Initializing database");
        executeScript(dataSource, "init_org_relations_test.sql");
    }

    @AfterEach
    void clearDB() {
        log.info("Clearing data");
        executeScript(dataSource, "clear_org_relations_test.sql");
    }

    private final Long APP_ID = 2_000_001L;
    private final Long MODEL1_ID = 3_000_001L;
    private final Long USER_ID = 2147483647L;

    @Test
    @DisplayName("Removal of model doesn't remove its parent app")
    void removalOfModelDoesNotDeleteItsParentAppAndOrganization() {
        val model = modelRepository.findById(MODEL1_ID).get();

        modelRepository.delete(model);

        assertThat(modelRepository.findById(MODEL1_ID)).isNotPresent();
        assertThat(appRepository.findById(APP_ID)).isPresent();
    }

    @Test
    @DisplayName("Removal of model share request doesn't remove its parent app")
    void modelShareRequestRemovalDoesNotAffectItsParents() {
        val modelShareRequestIdFromApp1 = UUID.fromString("22d7f072-cda0-4601-a95d-979fc37c67ce");
        val modelShareRequest = modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1);

        modelShareRequestRepository.delete(modelShareRequest);

        assertThat(modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1)).isNull();
        assertThat(appRepository.findById(APP_ID)).isPresent();
    }

    @Test
    @DisplayName("Removing user from app doesn't delete app itself")
    void removeUserFromAppDoesNotAffectAppAndParentOrg() {
        val entityManager = emf.createEntityManager();
        val transaction = entityManager.getTransaction();

        val hql = "select u from UserAppRole u where u.app.id = :appId and u.user.id=:userId";
        val query = entityManager
                .createQuery(hql)
                .setParameter("userId", USER_ID)
                .setParameter("appId", APP_ID);

        val userAppRole = query.getResultList().get(0);
        assertThat(userAppRole).isNotNull();

        transaction.begin();
        entityManager.remove(userAppRole);
        entityManager.flush();
        transaction.commit();

        assertThat(query.getResultList()).isEmpty();
        assertThat(appRepository.findById(APP_ID)).isPresent();
    }
}
