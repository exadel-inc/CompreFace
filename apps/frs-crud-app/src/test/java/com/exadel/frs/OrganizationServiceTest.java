package com.exadel.frs;

import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.dto.ui.OrgCreateDto;
import com.exadel.frs.dto.ui.OrgUpdateDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.entity.UserOrganizationRoleId;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.repository.OrganizationRepository;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import liquibase.integration.spring.SpringLiquibase;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class OrganizationServiceTest {

    private static final String ORGANIZATION_GUID = "org-guid";
    private static final Long USER_ID = 1L;
    private static final Long ADMIN_ID = 2L;
    private static final Long ORGANIZATION_ID = 3L;

    @Mock
    private UserService userServiceMock;

    @Mock
    private OrganizationRepository organizationRepositoryMock;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    private User user(Long id) {
        return User.builder()
                   .id(id)
                   .build();
    }

    private static Stream<Arguments> readRoles() {
        return Stream.of(
                Arguments.of(OrganizationRole.ADMINISTRATOR),
                Arguments.of(USER)
        );
    }

    private static Stream<Arguments> writeRoles() {
        return Stream.of(Arguments.of(OWNER));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void successGetOrganization(OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .build();

        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        val result = organizationService.getOrganization(ORGANIZATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(ORGANIZATION_ID);
    }

    @Test
    void successGetOrganizations() {
        when(organizationRepositoryMock.findAllByUserOrganizationRoles_Id_UserId(anyLong()))
                .thenReturn(List.of(Organization.builder().build()));

        val organizations = organizationService.getOrganizations(1L);

        assertThat(organizations).hasSize(1);
    }

    @Test
    void successGetOwnedOrganizations() {
        val owner = UserOrganizationRole.builder()
                                        .id(new UserOrganizationRoleId(1L, 1L))
                                        .user(User.builder().id(1L).build())
                                        .role(OWNER)
                                        .build();

        val admin = UserOrganizationRole.builder()
                                        .id(new UserOrganizationRoleId(1L, 2L))
                                        .user(User.builder().id(1L).build())
                                        .role(OrganizationRole.ADMINISTRATOR)
                                        .build();

        val user = UserOrganizationRole.builder()
                                       .id(new UserOrganizationRoleId(1L, 3L))
                                       .user(User.builder().id(1L).build())
                                       .role(USER)
                                       .build();

        val ownedOrg = Organization.builder()
                                   .guid(UUID.randomUUID().toString())
                                   .userOrganizationRoles(List.of(owner))
                                   .build();

        val notOwnedOrg1 = Organization.builder()
                                       .guid(UUID.randomUUID().toString())
                                       .userOrganizationRoles(List.of(admin))
                                       .build();

        val notOwnedOrg2 = Organization.builder()
                                       .guid(UUID.randomUUID().toString())
                                       .userOrganizationRoles(List.of(user))
                                       .build();

        when(organizationRepositoryMock.findAllByUserOrganizationRoles_Id_UserId(anyLong()))
                .thenReturn(List.of(ownedOrg, notOwnedOrg1, notOwnedOrg2));

        val organizations = organizationService.getOwnedOrganizations(1L);

        assertThat(organizations).hasSize(1);
        assertThat(organizations).contains(ownedOrg);
        assertThat(organizations).doesNotContain(notOwnedOrg1);
        assertThat(organizations).doesNotContain(notOwnedOrg2);
    }

    @Test
    void successCreateOrganization() {
        val orgCreateDto = OrgCreateDto.builder()
                                       .name("Organization")
                                       .build();

        val user = user(USER_ID);

        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        organizationService.createOrganization(orgCreateDto, USER_ID);

        val varArgs = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName()).isEqualTo(orgCreateDto.getName());
        assertThat(varArgs.getValue().getUserOrganizationRoles()).hasSize(1);
        assertThat(varArgs.getValue().getUserOrganizationRole(USER_ID).get().getRole()).isEqualTo(OWNER);
    }

    @Test
    void failCreateOrganizationNameIsNotUnique() {
        val orgCreateDto = OrgCreateDto.builder()
                                       .name("Organization")
                                       .build();

        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        assertThrows(
                NameIsNotUniqueException.class,
                () -> organizationService.createOrganization(orgCreateDto, null)
        );
    }

    @Test
    void successUpdateOrganization() {
        val orgUpdateDto = OrgUpdateDto.builder()
                                       .name("Organization 2")
                                       .build();

        val admin = user(ADMIN_ID);
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .name("Organization 1")
                                       .build();

        organization.addUserOrganizationRole(admin, OWNER);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.updateOrganization(orgUpdateDto, ORGANIZATION_GUID, ADMIN_ID);

        assertThat(organization.getName()).isEqualTo(orgUpdateDto.getName());
    }

    @Test
    void failUpdateOrganizationNameIsNotUnique() {
        val orgUpdateDto = OrgUpdateDto.builder()
                                       .name("Organization 2")
                                       .build();

        val user = user(USER_ID);
        val organization = Organization.builder()
                                       .name("Organization 1")
                                       .guid(ORGANIZATION_GUID)
                                       .build();

        organization.addUserOrganizationRole(user, OWNER);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));
        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        assertThrows(
                NameIsNotUniqueException.class,
                () -> organizationService.updateOrganization(orgUpdateDto, ORGANIZATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        val orgUpdateDto = OrgUpdateDto.builder()
                                       .name("Organization 2")
                                       .build();

        val user = user(USER_ID);
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .build();

        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        assertThrows(
                InsufficientPrivilegesException.class,
                () -> organizationService.updateOrganization(orgUpdateDto, ORGANIZATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateOrganizationSelfRoleChange(OrganizationRole organizationRole) {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(USER.toString())
                                                 .build();

        val user = user(USER_ID);
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .guid(ORGANIZATION_GUID)
                                       .build();

        organization.addUserOrganizationRole(user, organizationRole);

        val organizationUpdate = Organization.builder().build();
        organizationUpdate.addUserOrganizationRole(user, USER);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        assertThrows(
                SelfRoleChangeException.class,
                () -> organizationService.updateUserOrgRole(userRoleUpdateDto, ORGANIZATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteOrganization(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .build();

        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.deleteOrganization(ORGANIZATION_GUID, USER_ID);

        verify(organizationRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failDeleteOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .build();

        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        assertThrows(
                InsufficientPrivilegesException.class,
                () -> organizationService.deleteOrganization(ORGANIZATION_GUID, USER_ID)
        );
    }

    @DisplayName("Test organization delete")
    @ExtendWith(SpringExtension.class)
    @DataJpaTest
    @Nested
    @MockBeans({@MockBean(SpringLiquibase.class), @MockBean(PasswordEncoder.class), @MockBean(EmailSender.class)})
    @Import({OrganizationService.class, UserService.class})
    public class RemoveOrganizationTest {

        @Autowired
        private OrganizationRepository repository;

        @Autowired
        private AppRepository appRepository;

        @Autowired
        private ModelRepository modelRepository;

        @Autowired
        private AppModelRepository appModelRepository;

        @Autowired
        private ModelShareRequestRepository modelShareRequestRepository;

        @Autowired
        private OrganizationService service;

        @Autowired
        private TestEntityManager entityManager;

        private final String ORG_GUID = "d098a11e-c4e4-4f56-86b2-85ab3bc83044";
        private final Long ORG_ID = 1_000_001L;
        private final Long OTHER_ORG_ID = 1_000_002L;
        private final Long APP1_ID = 2_000_001L;
        private final Long APP2_ID = 2_000_002L;
        private final Long OTHER_APP_ID = 2_000_003L;
        private final Long MODEL1_ID = 3_000_001L;
        private final Long USER_ID = 25L;

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removes expected organization")
        public void removesExpectedOrganization() {
            assertThat(repository.findAll()).hasSize(2);
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(repository.findAll()).hasSize(1);
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isFalse();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removes all child apps")
        public void removesChildApps() {
            assertThat(appRepository.findAll()).hasSize(3);
            assertThat(appRepository.findAllByOrganizationId(ORG_ID)).hasSize(2);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(appRepository.findAll()).hasSize(1);
            assertThat(appRepository.findAllByOrganizationId(ORG_ID)).isEmpty();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Apps that do not belong to organization are not deleted")
        public void unrelatedAppsAreNotAffected() {
            assertThat(appRepository.findAllByOrganizationId(OTHER_ORG_ID)).hasSize(1);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(appRepository.findAllByOrganizationId(OTHER_ORG_ID)).hasSize(1);
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Models that owned by the organization are also deleted.")
        public void removesModelsThatBelongToOrganization() {
            assertEquals(3, modelRepository.findAll()
                                           .stream()
                                           .filter(m -> List.of(APP1_ID, APP2_ID).contains(m.getApp().getId()))
                                           .count());

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertEquals(0, modelRepository.findAll()
                                           .stream()
                                           .filter(m -> List.of(APP1_ID, APP2_ID).contains(m.getApp().getId()))
                                           .count());
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Models that don't belong to organization are not deleted even if they are shared with the organization")
        public void otherModelsAreNotAffected() {
            assertEquals(2, modelRepository.findAll()
                                           .stream()
                                           .filter(m -> List.of(OTHER_APP_ID).contains(m.getApp().getId()))
                                           .count());

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertEquals(2, modelRepository.findAll()
                                           .stream()
                                           .filter(m -> List.of(OTHER_APP_ID).contains(m.getApp().getId()))
                                           .count());
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("When models removed because of parent organization's removal they cannot be shared any more")
        public void removedModelsAreNotSharedAnyMore() {
            assertThat(appModelRepository.findAllByModelAppOrganizationId(ORG_ID)).hasSize(2);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(appModelRepository.findAllByModelAppOrganizationId(ORG_ID)).isEmpty();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Models shared to organization are not shared any more")
        public void modelsSharedToOrganizationsAppsAreNotSharedAnyMore() {
            assertThat(appModelRepository.findAllByAppOrganizationId(ORG_ID)).hasSize(2);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(appModelRepository.findAllByAppOrganizationId(ORG_ID)).isEmpty();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removes model share requests of organization's apps")
        public void removesModelShareRequests() {
            assertEquals(2, modelShareRequestRepository
                    .findAll()
                    .stream()
                    .filter(mShR -> List.of(APP1_ID, APP2_ID).contains(mShR.getApp().getId()))
                    .count());

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertEquals(0, modelShareRequestRepository
                    .findAll()
                    .stream()
                    .filter(mShR -> List.of(APP1_ID, APP2_ID).contains(mShR.getApp().getId()))
                    .count());
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removes user role mapping for organization")
        public void removesUserOrganizationMapping() {
            val hql = "select u from UserOrganizationRole u where u.organization.id = :orgId";
            val query = entityManager
                    .getEntityManager()
                    .createQuery(hql)
                    .setParameter("orgId", ORG_ID);

            assertThat(query.getResultList()).hasSize(1);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(query.getResultList()).isEmpty();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removes user role mapping for organization's apps")
        public void removes() {
            val hql = "select u from UserAppRole u where u.app.id in :appIds";
            val query = entityManager
                    .getEntityManager()
                    .createQuery(hql)
                    .setParameter("appIds", List.of(APP1_ID, APP2_ID));

            assertThat(query.getResultList()).hasSize(2);

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertThat(query.getResultList()).isEmpty();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removal of app doesn't remove its parent organization")
        public void removalOfAppDoesNotRemoveItsOrganization() {
            val app = appRepository.findById(APP1_ID).get();

            appRepository.delete(app);

            assertThat(appRepository.findById(APP1_ID).isPresent()).isFalse();
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removal of model doesn't remove its parent app and organization")
        public void removalOfModelDoesNotDeleteItsParentAppAndOrganization() {
            val model = modelRepository.findById(MODEL1_ID).get();

            modelRepository.delete(model);

            assertThat(modelRepository.findById(MODEL1_ID).isPresent()).isFalse();
            assertThat(appRepository.findById(APP1_ID).isPresent()).isTrue();
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removal of model share request doesn't remove its parent app and organization")
        public void modelShareRequestRemovalDoesNotAffectItsParents() {
            val modelShareRequestIdFromApp1 = UUID.fromString("22d7f072-cda0-4601-a95d-979fc37c67ce");
            val modelShareRequest = modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1);

            modelShareRequestRepository.delete(modelShareRequest);

            assertThat(modelShareRequestRepository.findModelShareRequestByRequestId(modelShareRequestIdFromApp1)).isNull();
            assertThat(appRepository.findById(APP1_ID).isPresent()).isTrue();
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removing user from organization doesn't delete organization itself")
        public void removeUserFromOrgDoesNotDeleteOrgItself() {
            val hql = "select u from UserOrganizationRole u where u.organization.id = :orgId and u.user.id=:userId";
            val query = entityManager
                    .getEntityManager()
                    .createQuery(hql)
                    .setParameter("userId", USER_ID)
                    .setParameter("orgId", ORG_ID);

            assertThat(query.getResultList()).hasSize(1);

            entityManager.remove(query.getResultList().get(0));
            entityManager.flush();

            assertThat(query.getResultList()).isEmpty();
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();
        }

        @Test
        @Sql("/init_remove_org_test.sql")
        @DisplayName("Removing user from app doesn't delete app itself and parent organization")
        public void removeUserFromAppDoesNotAffectAppAndParentOrg() {
            val hql = "select u from UserAppRole u where u.app.id = :appId and u.user.id=:userId";
            val query = entityManager
                    .getEntityManager()
                    .createQuery(hql)
                    .setParameter("userId", USER_ID)
                    .setParameter("appId", APP1_ID);

            assertThat(query.getResultList()).hasSize(1);

            entityManager.remove(query.getResultList().get(0));
            entityManager.flush();

            assertThat(query.getResultList()).isEmpty();
            assertThat(appRepository.findById(APP1_ID).isPresent()).isTrue();
            assertThat(repository.findByGuid(ORG_GUID).isPresent()).isTrue();
        }
    }
}