package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.domain.Face;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnabledIf(
        expression = "#{environment.acceptsProfiles('integration-test')}"
)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class FacesRepositoryTest {

    @Autowired
    private FacesRepository facesRepository;
    private final static String APP_GUID = "app_guid_for_test";
    private final static String APP_GUID_OTHER = "app_guid_other";

    @BeforeEach
    public void init() {
        var faceA = makeFace("A", APP_GUID);
        var faceB = makeFace("B", APP_GUID_OTHER);
        var faceC = makeFace("C", APP_GUID);

        facesRepository.saveAll(List.of(faceA, faceB, faceC));
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(final String name, final String appKey) {
        var face = new Face();
        face.setFaceName(name);
        face.setApiKey(appKey);
        face.setId("Id_" + name);

        return face;
    }

    @Test
    public void getAll() {
        var all = facesRepository.findAll();
        assertNotEquals(null, all);
        assertThat(all).hasSize(3);
    }

    @Test
    public void findNamesForApiGuid() {
        val expected = Arrays.asList("A", "C");
        val actual = facesRepository.findByApiKey(APP_GUID).stream()
                                                        .map(Face::getFaceName)
                                                        .collect(toList());

        assertThat(actual).isEqualTo(expected);
    }
}