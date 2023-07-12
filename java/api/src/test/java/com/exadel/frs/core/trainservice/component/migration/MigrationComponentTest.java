package com.exadel.frs.core.trainservice.component.migration;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MigrationComponentTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @MockBean
    FacesFeignClient feignClient;

    @Autowired
    MigrationComponent migrationComponent;

    @Test
    void testRecalculateEmbeddingsWithOutdatedCalculator() {
        var currentCalculator = "super-puper-calculator";
        var newEmbeddingArray = new Double[]{7.7, 36.6, 42.0};

        when(feignClient.getStatus())
                .thenReturn(new FacesStatusResponse().setCalculatorVersion(currentCalculator));
        when(feignClient.findFaces(any(), any(), any(), any(), any()))
                .thenReturn(FindFacesResponse.builder()
                        .result(List.of(FindFacesResult.builder().embedding(newEmbeddingArray).build()))
                        .build());

        final Model model = dbHelper.insertModel();
        final Subject subject = dbHelper.insertSubject(model, "subject1");

        // no image
        dbHelper.insertEmbeddingNoImg(subject);
        // with image; with current calculator
        dbHelper.insertEmbeddingWithImg(subject, currentCalculator);
        // with image; with outdated calculator
        var outdatedEmbedding = dbHelper.insertEmbeddingWithImg(subject, "outdatedCalculator");

        int recalculated = migrationComponent.recalculateEmbeddingsWithOutdatedCalculator();
        assertThat(recalculated).isEqualTo(1);

        final Embedding recalculatedEmbedding = embeddingRepository.findById(outdatedEmbedding.getId()).orElseThrow();
        assertThat(recalculatedEmbedding.getCalculator()).isEqualTo(currentCalculator);
        assertThat(recalculatedEmbedding.getEmbedding()).containsExactly(ArrayUtils.toPrimitive(newEmbeddingArray));
    }
}