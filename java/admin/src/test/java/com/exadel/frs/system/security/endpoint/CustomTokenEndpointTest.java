package com.exadel.frs.system.security.endpoint;

import static com.exadel.frs.system.global.Constants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.EmbeddedPostgreSQLTest;
import com.exadel.frs.FrsApplication;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@SpringBootTest(classes = FrsApplication.class)
class CustomTokenEndpointTest extends EmbeddedPostgreSQLTest {

    @Value("${spring.mail.enable}")
    private boolean isMailEnabled;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private UserService userService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        when(userService.generateRegistrationToken()).thenReturn("MockRegistrationToken");
        when(userService.hasOnlyDemoUser()).thenReturn(false);

        registerMockUser();
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldReturnAccessTokenCookieAndRefreshTokenCookieWhenUserSignIn() {

        mockMvc.perform(post(ADMIN + "/oauth/token")
                       .param("grant_type", "password")
                       .param("scope", "all")
                       .param("username", "mockuser@gmail.com")
                       .param("password", "password")
                       .with(httpBasic("CommonClientId", "password"))
                       .accept("application/json;charset=UTF-8"))
               .andExpect(status().isOk())
               .andExpect(cookie().exists("CFSESSION"))
               .andExpect(cookie().exists("REFRESH_TOKEN"))
               .andDo(print());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestStatusCodeWhenUserCredentialsAreInvalid() {

        mockMvc.perform(post(ADMIN + "/oauth/token")
                       .param("grant_type", "password")
                       .param("scope", "all")
                       .param("username", "invaliduser@gmail.com")
                       .param("password", "password")
                       .with(httpBasic("CommonClientId", "password"))
                       .accept("application/json;charset=UTF-8"))
               .andExpect(status().isBadRequest())
               .andExpect(cookie().doesNotExist("CFSESSION"))
               .andExpect(cookie().doesNotExist("REFRESH_TOKEN"))
               .andDo(print());
    }

    @Test
    @SneakyThrows
    void shouldReturnUnauthorizedStatusCodeWhenClientCredentialsAreInvalid() {

        mockMvc.perform(post(ADMIN + "/oauth/token")
                       .param("grant_type", "password")
                       .param("scope", "all")
                       .param("username", "mockuser@gmail.com")
                       .param("password", "password")
                       .with(httpBasic("InvalidClientId", "password"))
                       .accept("application/json;charset=UTF-8"))
               .andExpect(status().isUnauthorized())
               .andExpect(cookie().doesNotExist("CFSESSION"))
               .andExpect(cookie().doesNotExist("REFRESH_TOKEN"))
               .andDo(print());
    }

    @Test
    @SneakyThrows
    void shouldReturnNewAccessTokenAndNewRefreshTokenWhenRefreshTokenRequestOccurred() {

        var signInResult = mockMvc.perform(post(ADMIN + "/oauth/token")
                                          .param("grant_type", "password")
                                          .param("scope", "all")
                                          .param("username", "mockuser@gmail.com")
                                          .param("password", "password")
                                          .with(httpBasic("CommonClientId", "password"))
                                          .accept("application/json;charset=UTF-8"))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("CFSESSION"))
                                  .andExpect(cookie().exists("REFRESH_TOKEN"))
                                  .andDo(print());

        var accessTokenCookie = signInResult.andReturn().getResponse().getCookie("CFSESSION");
        var refreshTokenCookie = signInResult.andReturn().getResponse().getCookie("REFRESH_TOKEN");

        var refreshTokenResult = mockMvc.perform(post(ADMIN + "/oauth/token")
                                                .param("grant_type", "refresh_token")
                                                .param("scope", "all")
                                                .with(httpBasic("CommonClientId", "password"))
                                                .accept("application/json;charset=UTF-8")
                                                .cookie(refreshTokenCookie))
                                        .andExpect(status().isOk())
                                        .andExpect(cookie().exists("CFSESSION"))
                                        .andExpect(cookie().exists("REFRESH_TOKEN"))
                                        .andDo(print());

        var newAccessTokenCookie = refreshTokenResult.andReturn().getResponse().getCookie("CFSESSION");
        var newRefreshTokenCookie = refreshTokenResult.andReturn().getResponse().getCookie("REFRESH_TOKEN");

        assertThat(accessTokenCookie.getValue()).isNotEqualTo(newAccessTokenCookie.getValue());
        assertThat(refreshTokenCookie.getValue()).isNotEqualTo(newRefreshTokenCookie.getValue());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestStatusCodeWhenRefreshTokenRequestOccurredWithInvalidRefreshToken() {

        var refreshTokenCookie = new Cookie("REFRESH_TOKEN", "InvalidValue");

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath(ADMIN + "/oauth/token");
        refreshTokenCookie.setMaxAge(-1);

        mockMvc.perform(post(ADMIN + "/oauth/token")
                       .param("grant_type", "refresh_token")
                       .param("scope", "all")
                       .with(httpBasic("CommonClientId", "password"))
                       .accept("application/json;charset=UTF-8")
                       .cookie(refreshTokenCookie))
               .andExpect(status().isBadRequest())
               .andExpect(cookie().doesNotExist("CFSESSION"))
               .andExpect(cookie().doesNotExist("REFRESH_TOKEN"))
               .andDo(print());
    }

    @SneakyThrows
    private void registerMockUser() {

        var mockUser = User.builder()
                           .email("mockuser@gmail.com")
                           .firstName("firstName")
                           .lastName("lastName")
                           .password("password")
                           .build();

        mockMvc.perform(post(ADMIN + "/user/register")
                       .contentType("application/json")
                       .accept("application/json")
                       .content(objectMapper.writeValueAsString(mockUser)))
               .andExpect(status().isCreated())
               .andDo(print());

        if (isMailEnabled) {
            userService.confirmRegistration("MockRegistrationToken");
        }
    }
}
