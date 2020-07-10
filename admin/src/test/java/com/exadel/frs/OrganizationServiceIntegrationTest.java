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
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.repository.OrganizationRepository;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
public class OrganizationServiceIntegrationTest {

    @DisplayName("Test organization delete")
    @ExtendWith(SpringExtension.class)
    @SpringBootTest
    @Nested
    @MockBeans({@MockBean(SpringLiquibase.class), @MockBean(PasswordEncoder.class), @MockBean(EmailSender.class)})
    class OrgAppAndModelRelationshipTest {

        @Autowired
        private OrganizationRepository repository;

        @Autowired
        private AppRepository appRepository;

        @Autowired
        private ModelRepository modelRepository;

        @Autowired
        private ModelShareRequestRepository modelShareRequestRepository;

        @PersistenceUnit
        private EntityManagerFactory emf;

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

        private final String ORG_GUID = "d098a11e-c4e4-4f56-86b2-85ab3bc83044";
        private final Long ORG_ID = 1_000_001L;
        private final Long APP_ID = 2_000_001L;
        private final Long MODEL1_ID = 3_000_001L;
        private final Long USER_ID = 2147483647L;

        @Test
        @DisplayName("Removal of app doesn't remove its parent organization")
        void removalOfAppDoesNotRemoveItsOrganization() {
            val app = appRepository.findById(APP_ID).get();

            appRepository.delete(app);

            assertThat(appRepository.findById(APP_ID)).isNotPresent();
            assertThat(repository.findByGuid(ORG_GUID)).isPresent();
        }

        @Test
        @DisplayName("Removal of model doesn't remove its parent app and organization")
        void removalOfModelDoesNotDeleteItsParentAppAndOrganization() {
            val model = modelRepository.findById(MODEL1_ID).get();

            modelRepository.delete(model);

            assertThat(modelRepository.findById(MODEL1_ID)).isNotPresent();
            assertThat(appRepository.findById(APP_ID)).isPresent();
            assertThat(repository.findByGuid(ORG_GUID)).isPresent();
        }

        @Test
        @DisplayName("Removal of model share request doesn't remove its parent app and organization")
        void modelShareRequestRemovalDoesNotAffectItsParents() {
            val modelShareRequestIdFromApp1 = UUID.fromString("22d7f072-cda0-4601-a95d-979fc37c67ce");
            val modelShareRequest = modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1);

            modelShareRequestRepository.delete(modelShareRequest);

            assertThat(modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1)).isNull();
            assertThat(appRepository.findById(APP_ID)).isPresent();
            assertThat(repository.findByGuid(ORG_GUID)).isPresent();
        }

        @Test
        @DisplayName("Removing user from organization doesn't delete organization itself")
        void removeUserFromOrgDoesNotDeleteOrgItself() {
            val entityManager = emf.createEntityManager();
            val transaction = entityManager.getTransaction();

            val hql = "select u from UserOrganizationRole u where u.organization.id = :orgId and u.user.id=:userId";
            val query = entityManager
                    .createQuery(hql)
                    .setParameter("userId", USER_ID)
                    .setParameter("orgId", ORG_ID);

            val userOrgRole = query.getResultList().get(0);
            assertThat(userOrgRole).isNotNull();

            transaction.begin();
            entityManager.remove(userOrgRole);
            entityManager.flush();
            transaction.commit();

            assertThat(query.getResultList()).isEmpty();
            assertThat(repository.findByGuid(ORG_GUID)).isPresent();
        }

        @Test
        @DisplayName("Removing user from app doesn't delete app itself and parent organization")
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
            assertThat(repository.findByGuid(ORG_GUID)).isPresent();
        }
    }
}
