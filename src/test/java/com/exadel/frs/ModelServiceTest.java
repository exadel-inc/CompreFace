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
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        Model result = modelService.getModel(modelId, userId);

        assertThat(result.getId(), is(modelId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetModelOrganizationUser(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        Model result = modelService.getModel(modelId, userId);

        assertThat(result.getId(), is(modelId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failGetModelInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.getModel(modelId, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successGetModels(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        List<Model> result = modelService.getModels(appId, userId);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetModelsOrganizationUser(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        List<Model> result = modelService.getModels(appId, userId);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failGetModelsInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.getModels(appId, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successCreateModel(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        modelService.createModel(model, userId);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(model.getGuid(), not(isEmptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateModelNameIsNotUnique(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(anyLong())).thenReturn(app);
        when(modelRepositoryMock.findByNameAndAppId(anyString(), anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.createModel(model, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failCreateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .name("name")
                .app(app)
                .build();

        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.createModel(model, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateModelEmptyName(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .app(app)
                .build();

        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> modelService.createModel(model, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successUpdateModel(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .name("name")
                .guid("guid")
                .app(app)
                .build();
        repoModel.addAppModelAccess(app, AppModelAccess.READONLY);

        Model model = Model.builder()
                .name("new_name")
                .guid("new_guid")
                .build();
        model.addAppModelAccess(app, AppModelAccess.TRAIN);

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        modelService.updateModel(modelId, model, userId);

        verify(modelRepositoryMock).save(any(Model.class));

        assertThat(repoModel.getName(), is(model.getName()));
        assertThat(repoModel.getGuid(), not(model.getGuid()));
        assertThat(repoModel.getAppModelAccess().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateModelNameIsNotUnique(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .name("name")
                .app(app)
                .build();

        Model model = Model.builder()
                .name("new_name")
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);
        when(modelRepositoryMock.findByNameAndAppId(anyString(), anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> modelService.updateModel(modelId, model, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failUpdateModelInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model repoModel = Model.builder()
                .name("name")
                .app(app)
                .build();

        Model model = Model.builder()
                .name("new_name")
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyLong())).thenReturn(app);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.updateModel(modelId, model, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateModelOrganizationMismatch(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId1 = 1L;
        Long organizationId2 = 2L;

        User user = user(userId);

        Organization organization1 = organization(organizationId1);
        organization1.addUserOrganizationRole(user, organizationRole);

        App app1 = App.builder()
                .id(appId)
                .organization(organization1)
                .build();

        Model repoModel = Model.builder()
                .app(app1)
                .build();

        Organization organization2 = organization(organizationId2);
        App app2 = App.builder()
                .id(appId)
                .organization(organization2)
                .build();

        Model model = Model.builder()
                .app(app2)
                .build();
        model.addAppModelAccess(app2, AppModelAccess.TRAIN);

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyLong())).thenReturn(app2);

        Assertions.assertThrows(OrganizationMismatchException.class, () -> modelService.updateModel(modelId, model, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successRegenerateGuid(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .guid("guid")
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        modelService.regenerateGuid(appId, userId);

        assertThat(model.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .guid("guid")
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.regenerateGuid(appId, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successDeleteModel(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .guid("guid")
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        modelService.deleteModel(appId, userId);

        verify(modelRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failDeleteModelInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long modelId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        Model model = Model.builder()
                .id(modelId)
                .guid("guid")
                .app(app)
                .build();

        when(modelRepositoryMock.findById(anyLong())).thenReturn(Optional.of(model));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> modelService.deleteModel(appId, userId));
    }

}
