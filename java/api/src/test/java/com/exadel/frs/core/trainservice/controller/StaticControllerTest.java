package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@IntegrationTest
@AutoConfigureMockMvc
class StaticControllerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    ImgRepository imgRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getImg() throws Exception {
        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subject");
        var embedding = dbHelper.insertEmbeddingWithImg(subject);

        var imgOptional = imgRepository.findById(embedding.getImg().getId());
        assertThat(imgOptional).isPresent();

        var img = imgOptional.get();
        assertThat(img.getContent()).isNotNull().isNotEmpty();

        mockMvc.perform(
                get(API_V1 + "/static/{apiKey}/images/{emgeddingId}", model.getApiKey(), embedding.getId())
        ).andExpect(content().bytes(img.getContent()));
    }
}