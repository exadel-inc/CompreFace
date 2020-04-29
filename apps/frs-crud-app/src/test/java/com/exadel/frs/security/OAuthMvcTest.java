package com.exadel.frs.security;

import com.exadel.frs.FrsApplication;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.mapper.OrganizationMapper;
import com.exadel.frs.mapper.UserOrgRoleMapper;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(profiles = "local-test")
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = FrsApplication.class)
@MockBeans({@MockBean(EmailSender.class), @MockBean(OrganizationService.class)})
public class OAuthMvcTest {

    private String registrationToken = UUID.randomUUID().toString();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @SpyBean
    private UserService userService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain).build();
        when(userService.generateRegistrationToken()).thenReturn(registrationToken);
    }

    private String obtainAccessToken(String username, String password) throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "CommonClientId");
        params.add("username", username);
        params.add("password", password);

        ResultActions result
                = mockMvc.perform(post("/oauth/token")
                .params(params)
                .with(httpBasic("CommonClientId", "password"))
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

    @Test
    public void availableOnlyWithAccessToken() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized());

        createUser("test1@email.com");

        var accessToken = obtainAccessToken("test1@email.com", "test1");
        mockMvc.perform(get("/user/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void ignoresCaseWhenLogin() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized());

        createUser("user@email.com");

        val accessToken = obtainAccessToken("User@EmaiL.com", "test1");

        mockMvc.perform(get("/user/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private void createUser(String email) throws Exception {
        val employeeString = "{\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"firstName\": \"test1\",\n" +
                "  \"id\": null,\n" +
                "  \"lastName\": \"test1\",\n" +
                "  \"password\": \"test1\",\n" +
                "  \"username\": \"test1\"\n" +
                "}";


        mockMvc.perform(post("/user/register")
                .contentType("application/json")
                .content(employeeString)
                .accept("application/json"))
                .andExpect(status().isCreated());

        confirmRegistration();
    }

    private void confirmRegistration() {
        userService.confirmRegistration(registrationToken);
    }
}