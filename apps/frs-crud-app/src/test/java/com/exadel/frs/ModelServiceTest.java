package com.exadel.frs;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.system.python.CoreDeleteFacesClient;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class ModelServiceTest {

    private static final String MODEL_GUID = "model-guid";
    private static final String APPLICATION_GUID = "app-guid";
    private static final String ORGANIZATION_GUID = "org-guid";
    private static final Long USER_ID = 1L;
    private static final Long MODEL_ID = 2L;
    private static final Long APPLICATION_ID = 3L;
    private static final Long ORGANIZATION_ID = 4L;

    private AppService appServiceMock;
    private ModelRepository modelRepositoryMock;
    private ModelService modelService;
    private ModelShareRequestRepository modelShareRequestRepository;
    private AppModelRepository appModelRepository;
    private CoreDeleteFacesClient facesClient;

    ModelServiceTest() {
        modelRepositoryMock = mock(ModelRepository.class);
        appServiceMock = mock(AppService.class);
        modelShareRequestRepository = mock(ModelShareRequestRepository.class);
        appModelRepository = mock(AppModelRepository.class);
        facesClient = mock(CoreDeleteFacesClient.class);
        modelService = new ModelService(modelRepositoryMock, appServiceMock, modelShareRequestRepository, appModelRepository, facesClient);
    }

    private User user(final Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Organization organization(final Long id) {
        return Organization.builder()
                .id(id)
                .guid(ORGANIZATION_GUID)
                .build();
    }

    private static Stream<Arguments> writeRoles() {
        return Stream.of(Arguments.of(OrganizationRole.OWNER),
                Arguments.of(OrganizationRole.ADMINISTRATOR));
    }

    private static Stream<Arguments> readRoles() {
        return Stream.of(Arguments.of(OrganizationRole.USER));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetModel(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        val result = modelService.getModel(MODEL_GUID);

        assertThat(result.getGuid(), is(MODEL_GUID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetModelOrganizationUser(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        val result = modelService.getModel(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID);

        assertThat(result.getId(), is(MODEL_ID));
        assertThat(result.getGuid(), is(MODEL_GUID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetModelWithUnknownAppGuid(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        val model = Model.builder()
                         .id(MODEL_ID)
                         .guid(MODEL_GUID)
                         .app(app)
                         .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        assertThrows(ModelDoesNotBelongToAppException.class, () ->
                modelService.getModel(ORGANIZATION_GUID, randomAlphabetic(10), MODEL_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetModelInsufficientPrivileges(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        assertThrows(InsufficientPrivilegesException.class, () ->
                modelService.getModel(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetModels(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        val result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetModelsOrganizationUser(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        val model = Model.builder()
                .id(MODEL_ID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        val result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetModelsInsufficientPrivileges(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        assertThrows(InsufficientPrivilegesException.class, () -> modelService.getModels(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successCreateModel(OrganizationRole organizationRole) {
        val modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(Model.class);
        verify(modelRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName(), is(modelCreateDto.getName()));
        assertThat(varArgs.getValue().getGuid(), not(emptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateModelNameIsNotUnique(OrganizationRole organizationRole) {
        val modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThrows(NameIsNotUniqueException.class, () ->
                modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failCreateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        val modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        assertThrows(InsufficientPrivilegesException.class, () ->
                modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateModelEmptyName(OrganizationRole organizationRole) {
        val modelCreateDto = ModelCreateDto.builder()
                .name("")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        assertThrows(EmptyRequiredFieldException.class, () ->
                modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUpdateModel(OrganizationRole organizationRole) {
        ModelUpdateDto modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();
        repoModel.addAppModelAccess(app, AppModelAccess.READONLY);

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.updateModel(modelUpdateDto, ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(repoModel.getName(), is(modelUpdateDto.getName()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateModelNameIsNotUnique(OrganizationRole organizationRole) {
        val modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThrows(NameIsNotUniqueException.class, () ->
                modelService.updateModel(modelUpdateDto, ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        val modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val repoModel = Model.builder()
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        assertThrows(InsufficientPrivilegesException.class, () ->
                modelService.updateModel(modelUpdateDto, ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successRegenerateGuid(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        modelService.regenerateApiKey(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID);

        assertThat(model.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        assertThrows(InsufficientPrivilegesException.class, () ->
                modelService.regenerateApiKey(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteModel(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        modelService.deleteModel(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(facesClient).deleteFaces(anyString());
        verify(modelRepositoryMock).findByGuid(anyString());
        verify(modelRepositoryMock).deleteById(anyLong());
        verifyNoMoreInteractions(facesClient, modelRepositoryMock);
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failDeleteModelInsufficientPrivileges(OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        assertThrows(InsufficientPrivilegesException.class, () ->
                modelService.deleteModel(ORGANIZATION_GUID, APPLICATION_GUID, MODEL_GUID, USER_ID)
        );

        verify(modelRepositoryMock).findByGuid(anyString());
        verifyNoMoreInteractions(facesClient, modelRepositoryMock);
    }
}