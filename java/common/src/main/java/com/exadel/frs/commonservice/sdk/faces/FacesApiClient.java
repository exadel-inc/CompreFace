package com.exadel.frs.commonservice.sdk.faces;

import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface representing Client for Faces API.
 */
public interface FacesApiClient {

    /**
     * Calls /find_faces endpoint of Faces API
     *
     * @param photo       - A picture with at least one face
     * @param faceLimit   - The limit of faces that you want recognized. Value of 0 represents no limit
     * @param thresholdC  - The minimum required confidence that a found face is actually a face
     * @param facePlugins - Comma-separated slugs of face plugins. Empty value - face plugins disabled, returns only bounding boxes
     * @return result of the operation
     */
    FindFacesResponse findFaces(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins,
            Boolean detectFaces);

    /**
     * Calls /find_faces_base64 endpoint of Faces API
     *
     * @param imageAsBase64 - A picture (as base64 string) with at least one face
     * @param faceLimit     - The limit of faces that you want recognized. Value of 0 represents no limit
     * @param thresholdC    - The minimum required confidence that a found face is actually a face
     * @param facePlugins   - Comma-separated slugs of face plugins. Empty value - face plugins disabled, returns only bounding boxes
     * @return result of the operation
     */
    FindFacesResponse findFacesBase64(
            String imageAsBase64,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins,
            Boolean detectFaces);

    /**
     * Calls /find_faces endpoint of Faces API with 'calculator' plugin always on
     */
    FindFacesResponse findFacesWithCalculator(
            MultipartFile photo,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins,
            Boolean detectFaces);

    /**
     * Calls /find_faces endpoint of Faces API with 'calculator' plugin always on
     */
    FindFacesResponse findFacesBase64WithCalculator(
            String imageAsBase64,
            Integer faceLimit,
            Double thresholdC,
            String facePlugins,
            Boolean detectFaces);

    /**
     * Calls /status endpoint of Faces API
     *
     * @return result of operation
     */
    FacesStatusResponse getStatus();
}
