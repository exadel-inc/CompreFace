package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.entity.Face;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ScanService {

    Face scanAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException;
}