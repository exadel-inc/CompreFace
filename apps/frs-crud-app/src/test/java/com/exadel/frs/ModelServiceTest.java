package com.exadel.frs;

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
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

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

    ModelServiceTest() {
        modelRepositoryMock = mock(ModelRepository.class);
        appServiceMock = mock(AppService.class);
        modelShareRequestRepository = mock(ModelShareRequestRepository.class);
        appModelRepository = mock(AppModelRepository.class);
        modelService = new ModelService(modelRepositoryMock, appServiceMock, modelShareRequestRepository, appModelRepository);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Organization organization(Long id) {
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
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        Model result = modelService.getModel(MODEL_GUID, USER_ID);

        assertThat(result.getGuid(), is(MODEL_GUID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetModelOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        Model result = modelService.getModel(MODEL_GUID, USER_ID);

        assertThat(result.getId(), is(MODEL_ID));
        assertThat(result.getGuid(), is(MODEL_GUID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetModelInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.getModel(MODEL_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetModels(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        List<Model> result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetModelsOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        Model model = Model.builder()
                .id(MODEL_ID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        List<Model> result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetModelsInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.getModels(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successCreateModel(OrganizationRole organizationRole) {
        ModelCreateDto modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID);

        ArgumentCaptor<Model> varArgs = ArgumentCaptor.forClass(Model.class);
        verify(modelRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName(), is(modelCreateDto.getName()));
        assertThat(varArgs.getValue().getGuid(), not(emptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateModelNameIsNotUnique(OrganizationRole organizationRole) {
        ModelCreateDto modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failCreateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        ModelCreateDto modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateModelEmptyName(OrganizationRole organizationRole) {
        ModelCreateDto modelCreateDto = ModelCreateDto.builder()
                .name("")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> modelService.createModel(modelCreateDto, ORGANIZATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUpdateModel(OrganizationRole organizationRole) {
        ModelUpdateDto modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();
        repoModel.addAppModelAccess(app, AppModelAccess.READONLY);

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.updateModel(modelUpdateDto, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(repoModel.getName(), is(modelUpdateDto.getName()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateModelNameIsNotUnique(OrganizationRole organizationRole) {
        ModelUpdateDto modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.updateModel(modelUpdateDto, MODEL_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        ModelUpdateDto modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.updateModel(modelUpdateDto, MODEL_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successRegenerateGuid(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        modelService.regenerateApiKey(MODEL_GUID, USER_ID);

        assertThat(model.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.regenerateApiKey(MODEL_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteModel(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        modelService.deleteModel(MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failDeleteModelInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANIZATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.deleteModel(MODEL_GUID, USER_ID));
    }

}
