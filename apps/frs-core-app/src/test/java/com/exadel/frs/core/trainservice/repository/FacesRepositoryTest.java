package com.exadel.frs.core.trainservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import com.exadel.frs.core.trainservice.TrainServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest(classes = TrainServiceApplication.class)
@EnabledIf(
        expression = "#{environment.acceptsProfiles('integration-test')}"
)
class FacesRepositoryTest {

    @Autowired
    private FacesRepository facesRepository;

    @Test
    public void getAll() {
        var all = facesRepository.findAll();

        assertNotEquals(null, all);
        assertThat(all).hasSizeGreaterThan(0);
    }
}