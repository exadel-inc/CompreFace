package com.exadel.frs.core.trainservice.service;

import static java.math.RoundingMode.HALF_UP;
import com.exadel.frs.commonservice.exception.WrongEmbeddingCountException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.EmbeddingVerificationProcessResult;
import com.exadel.frs.core.trainservice.dto.EmbeddingsVerificationProcessResponse;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

@Service("verificationService")
public class EmbeddingsVerificationProcessServiceImpl extends FaceVerificationProcessServiceImpl implements EmbeddingsProcessService {

    private static final int MINIMUM_EMBEDDING_COUNT = 2;

    private final FaceClassifierPredictor classifierPredictor;

    public EmbeddingsVerificationProcessServiceImpl(final FaceClassifierPredictor classifierPredictor,
                                                    final FacesApiClient client,
                                                    final ImageExtensionValidator imageValidator,
                                                    final FacesMapper mapper) {
        super(classifierPredictor, client, imageValidator, mapper);
        this.classifierPredictor = classifierPredictor;
    }

    @Override
    public EmbeddingsVerificationProcessResponse processEmbeddings(final ProcessEmbeddingsParams processEmbeddingsParams) {
        double[][] embeddings = processEmbeddingsParams.getEmbeddings();
        if (embeddings == null || (embeddings.length < MINIMUM_EMBEDDING_COUNT)) {
            int embeddingCount = embeddings == null ? 0 : embeddings.length;
            throw new WrongEmbeddingCountException(MINIMUM_EMBEDDING_COUNT, embeddingCount);
        }

        double[] source = embeddings[0];
        double[][] targets = ArrayUtils.subarray(embeddings, 1, embeddings.length);
        double[] similarities = classifierPredictor.verify(source, targets);

        List<EmbeddingVerificationProcessResult> results =
                IntStream.range(0, targets.length)
                         .mapToObj(i -> processEmbedding(targets[i], similarities[i]))
                         .sorted((e1, e2) -> Float.compare(e2.getSimilarity(), e1.getSimilarity()))
                         .collect(Collectors.toList());

        return new EmbeddingsVerificationProcessResponse(results);
    }

    private EmbeddingVerificationProcessResult processEmbedding(final double[] target, final double similarity) {
        BigDecimal scaledSimilarity = BigDecimal.valueOf(similarity).setScale(5, HALF_UP);
        return new EmbeddingVerificationProcessResult(
                target,
                scaledSimilarity.floatValue()
        );
    }
}
