package com.exadel.frs;

import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ClientRepository;
import com.exadel.frs.repository.ModelRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@Ignore
public class FrsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppRepository appRepository;

    @MockBean
    private ModelRepository modelRepository;

    @MockBean
    private ClientRepository clientRepository;

    @Before
    public void initialize() {
    }

    @Test
    public void getModels() throws Exception {
        mockMvc.perform(get("/models/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getApps() throws Exception {
        mockMvc.perform(get("/apps/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

}
