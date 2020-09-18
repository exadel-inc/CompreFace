package com.exadel.frs.core.trainservice.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.core.trainservice.component.FaceClassifierAdapter;
import com.exadel.frs.core.trainservice.component.FaceClassifierLockManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.TrainedModelDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({RetrainServiceImpl.class, FaceClassifierManager.class, TrainedModelDao.class, FaceDao.class, FaceClassifierLockManager.class,
        FaceClassifierAdapter.class})
public class RetrainServiceImplTestIT {

    @Autowired
    private RetrainService retrainService;

    @Autowired
    private FaceDao faceDao;

    @Autowired
    private FacesRepository facesRepository;

    @Autowired
    private FaceClassifierLockManager lockManager;

    @MockBean
    private FaceClassifierAdapter faceClassifierAdapter;

    private static final String MODEL_KEY_1 = randomUUID().toString();
    private static final String MODEL_KEY_2 = randomUUID().toString();
    private static final String MODEL_KEY_3 = randomUUID().toString();

    @BeforeEach
    void setUp() {
        val faceA = makeFace("A", MODEL_KEY_1);
        val faceB = makeFace("B", MODEL_KEY_2);
        val faceC = makeFace("C", MODEL_KEY_3);
        val faceD = makeFace("D", MODEL_KEY_1);

        facesRepository.saveAll(List.of(faceA, faceB, faceC, faceD));
    }

    @Test
    public void startRetrain() {
        retrainService.startRetrain(MODEL_KEY_1);
    }

    @Test
    void isTrainingRunTrue() {
        lockManager.lock(MODEL_KEY_2);
        val actual = retrainService.isTrainingRun(MODEL_KEY_2);

        assertThat(actual).isTrue();
    }

    @Test
    void isTrainingRunFalse() {
        val actual = retrainService.isTrainingRun(MODEL_KEY_3);

        assertThat(actual).isFalse();
    }

    @Test
    void abortTraining() {
        retrainService.abortTraining(MODEL_KEY_1);
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    private static Face makeFace(final String name, final String modelApiKey) {
        return Face.builder()
                   .faceName(name)
                   .apiKey(modelApiKey)
                   .faceImg("hex-string-1".getBytes())
                   .rawImg("hex-string-2".getBytes())
                   .id(randomUUID().toString())
                   .embedding(Face.Embedding.builder()
                                            .embeddings(List.of(1.0, 2.0, 3.0))
                                            .calculatorVersion("1.0")
                                            .build()
                   )
                   .build();
    }
}
