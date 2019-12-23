package com.exadel.frs;

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
import com.exadel.frs.exception.OrganizationMismatchException;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class ModelServiceTest {

    private static final String MODEL_GUID = "model-guid";
    private static final String APPLICATION_GUID = "application-guid";
    private static final Long USER_ID = 1L;
    private static final Long MODEL_ID = 2L;
    private static final Long APPLICATION_ID = 3L;
    private static final Long ORGANIZATION_ID = 4L;

    private AppService appServiceMock;
    private ModelRepository modelRepositoryMock;
    private ModelService modelService;

    public ModelServiceTest() {
        modelRepositoryMock = mock(ModelRepository.class);
        appServiceMock = mock(AppService.class);
        modelService = new ModelService(modelRepositoryMock, appServiceMock);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Organization organization(Long id) {
        return Organization.builder()
                .id(id)
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
    public void successGetModel(OrganizationRole organizationRole) {
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
    public void successGetModelOrganizationUser(OrganizationRole organizationRole) {
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
    public void failGetModelInsufficientPrivileges(OrganizationRole organizationRole) {
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
    public void successGetModels(OrganizationRole organizationRole) {
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
    public void successGetModelsOrganizationUser(OrganizationRole organizationRole) {
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
    public void failGetModelsInsufficientPrivileges(OrganizationRole organizationRole) {
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
    public void successCreateModel(OrganizationRole organizationRole) {
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
                .guid(MODEL_GUID)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.createModel(model, USER_ID);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(model.getGuid(), not(isEmptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateModelNameIsNotUnique(OrganizationRole organizationRole) {
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
                .guid(MODEL_GUID)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.findByNameAndAppId(anyString(), anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.createModel(model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failCreateModelInsufficientPrivileges(OrganizationRole organizationRole) {
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
                .guid(MODEL_GUID)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.createModel(model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateModelEmptyName(OrganizationRole organizationRole) {
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

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> modelService.createModel(model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successUpdateModel(OrganizationRole organizationRole) {
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

        Model model = Model.builder()
                .name("new_name")
                .guid("new_guid")
                .build();
        model.addAppModelAccess(app, AppModelAccess.TRAIN);

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        modelService.updateModel(MODEL_GUID, model, USER_ID);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(repoModel.getName(), is(model.getName()));
        assertThat(repoModel.getGuid(), not(model.getGuid()));
        assertThat(repoModel.getAppModelAccess().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateModelNameIsNotUnique(OrganizationRole organizationRole) {
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

        Model model = Model.builder()
                .name("new_name")
                .build();

        when(modelRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.findByNameAndAppId(anyString(), anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.updateModel(MODEL_GUID, model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failUpdateModelInsufficientPrivileges(OrganizationRole organizationRole) {
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

        Model model = Model.builder()
                .name("new_name")
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.updateModel(MODEL_GUID, model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateModelOrganizationMismatch(OrganizationRole organizationRole) {
        Long organizationId1 = 1L;
        Long organizationId2 = 2L;

        User user = user(USER_ID);

        Organization organization1 = organization(organizationId1);
        organization1.addUserOrganizationRole(user, organizationRole);

        App app1 = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization1)
                .build();

        Model repoModel = Model.builder()
                .guid(MODEL_GUID)
                .app(app1)
                .build();

        Organization organization2 = organization(organizationId2);
        App app2 = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization2)
                .build();

        Model model = Model.builder()
                .app(app2)
                .build();
        model.addAppModelAccess(app2, AppModelAccess.TRAIN);

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app2);

        Assertions.assertThrows(OrganizationMismatchException.class, () -> modelService.updateModel(MODEL_GUID, model, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successRegenerateGuid(OrganizationRole organizationRole) {
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
    public void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
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
    public void successDeleteModel(OrganizationRole organizationRole) {
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
    public void failDeleteModelInsufficientPrivileges(OrganizationRole organizationRole) {
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
