package com.exadel.frs.core.trainservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.exadel.frs.core.trainservice.TrainServiceApplication;
import com.exadel.frs.core.trainservice.domain.Face;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.List;

@SpringBootTest(classes = TrainServiceApplication.class)
@EnabledIf(
    expression = "#{environment.acceptsProfiles('integration-test')}"
)
class FacesRepositoryTest {

  @Autowired
  private FacesRepository facesRepository;

  @Test
  public void getAll() {
    List<Face> all = facesRepository.findAll();
    Assertions.assertNotEquals(null, all);
    assertThat(all).hasSizeGreaterThan(0);
  }

}