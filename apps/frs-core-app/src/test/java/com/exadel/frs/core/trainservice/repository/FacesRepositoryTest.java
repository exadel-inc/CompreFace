package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.domain.Face;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIf(
    expression = "#{environment.acceptsProfiles('integration-test')}"
)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class FacesRepositoryTest {
    @Autowired
    private FacesRepository facesRepository;
    private final String APP_GUID = "app_guid_for_test";
    private final String APP_GUID_OTHER = "app_guid_other";

    @BeforeEach
    public void init() {
        Face faceA = makeFace("A", APP_GUID);
        Face faceB = makeFace("B", APP_GUID_OTHER);
        Face faceC = makeFace("C", APP_GUID);
        facesRepository.save(faceA);
        facesRepository.save(faceB);
        facesRepository.save(faceC);
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(String name, String appKey) {
        Face face = new Face();
        face.setFaceName(name);
        face.setApiKey(appKey);
        face.setId("Id_" + name);
        return face;
    }

    @Test
    public void getAll() {
        List<Face> all = facesRepository.findAll();
        Assertions.assertNotEquals(null, all);
        assertThat(all).hasSize(3);
    }

    @Test
    public void findNamesForApiGuid() throws Exception {
        List<String> expectedOutput = Arrays.asList("A", "C");
        List<String> actualResult = facesRepository.findByApiKey(APP_GUID).stream().map(Face::getFaceName).collect(Collectors.toList());
        assertEquals(expectedOutput, actualResult);
        //Test with different value
        actualResult = facesRepository.findByApiKey(APP_GUID_OTHER).stream().map(Face::getFaceName).collect(Collectors.toList());
        expectedOutput = Arrays.asList("B");
        assertEquals(expectedOutput, actualResult);

    }

}
