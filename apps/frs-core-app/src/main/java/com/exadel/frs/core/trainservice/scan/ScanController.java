package com.exadel.frs.core.trainservice.scan;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/scan-faces")
@RequiredArgsConstructor
public class ScanController {
    private final ScanService scanService;
    private final PythonClient pythonClient;

    @PostMapping
    public void scanFaces(
            @ApiParam(value = "A picture with at least one face (accepted formats: jpeg, png).", required = true)
            @RequestParam MultipartFile photo,
            @ApiParam(value = "The limit of faces that you want recognized. Value of 0 represents no limit.", required = true)
            @RequestParam Integer limit,
            @ApiParam(value = "Third threshold parameter in a three step (A-B-C) thresholding in face detection. Decrease this value if faces are not detected. Valid values are in the range (0;1).")
            @RequestParam(required = false) Double thresholdC) {
        //call to python
        var scanResponse = pythonClient.scanFaces(photo, limit, thresholdC);
        //save result
        System.out.println(scanResponse);

    }
}
