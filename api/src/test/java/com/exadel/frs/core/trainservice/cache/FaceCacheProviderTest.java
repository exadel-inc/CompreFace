package com.exadel.frs.core.trainservice.cache;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class FaceCacheProviderTest {

    @Mock
    private FaceDao faceDao;

    @InjectMocks
    private FaceCacheProvider faceCacheProvider;

    private static final String API_KEY = "model_key";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getOrLoad() {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY)
        );
        when(faceDao.findAllFacesByApiKey(API_KEY)).thenReturn(faces);

        val actual = faceCacheProvider.getOrLoad(API_KEY);

        verify(faceDao).findAllFacesByApiKey(API_KEY);
        verifyNoMoreInteractions(faceDao);

        assertThat(actual).isNotNull();
        assertThat(actual.getFaces()).isNotNull();
        assertThat(actual.getFaces().size()).isEqualTo(faces.size());
    }
}
