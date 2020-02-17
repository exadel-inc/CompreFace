package com.exadel.frs.core.trainservice.scan;

import org.springframework.web.multipart.MultipartFile;

public interface ScanService {
    void scanAndSaveFace(MultipartFile file, String faceName, Double detProbThreshold, String xApiKey);
}
