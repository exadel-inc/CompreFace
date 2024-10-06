package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.FaceProcessResponse;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;

public interface FaceProcessService {

    FaceProcessResponse processImage(ProcessImageParams processImageParams);
}
