package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.EmbeddingsProcessResponse;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;

public interface EmbeddingsProcessService extends FaceProcessService {

    EmbeddingsProcessResponse processEmbeddings(ProcessEmbeddingsParams processEmbeddingsParams);
}
