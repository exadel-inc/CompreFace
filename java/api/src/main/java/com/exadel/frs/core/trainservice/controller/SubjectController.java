package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.dto.*;
import com.exadel.frs.core.trainservice.mapper.EmbeddingMapper;
import com.exadel.frs.core.trainservice.service.SubjectService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;

import static com.exadel.frs.core.trainservice.system.global.Constants.*;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(API_V1 + "/recognition/faces/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
    private final ImageExtensionValidator imageValidator;
    private final EmbeddingMapper embeddingMapper;

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping
    public FaceResponseDto addFaces(
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true) @RequestParam final MultipartFile file,
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(SUBJECT) final String subjectName,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey
    ) throws IOException {
        imageValidator.validate(file);

        final Pair<Subject, Embedding> pair = subjectService.findAndSaveSubject(
                file,
                subjectName,
                detProbThreshold,
                apiKey
        );

        return new FaceResponseDto(pair.getRight().getId().toString(), subjectName);
    }

    @GetMapping
    public Map<String, Collection<FaceResponseDto>> findFacesByModel(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return Map.of(
                SUBJECTS,
                embeddingMapper.toResponseDto(subjectService.findSubjects(apiKey))
        );
    }

    @WriteEndpoint
    @DeleteMapping
    public List<FaceResponseDto> deleteSubject(
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(name = SUBJECT, required = false) final String subjectName,
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        if (StringUtils.isBlank(subjectName)) {
            subjectService.deleteSubjectsByApiKey(apiKey);
        } else {
            // TODO
            subjectService.deleteSubjectByName(apiKey, subjectName);
        }

        return Collections.emptyList();
    }

    @WriteEndpoint
    @PutMapping
    public Map<String, Object> updateSubject(
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(name = SUBJECT) final @NotBlank String subject,
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "New " + SUBJECT_DESC, required = true) @Valid @RequestBody final UpdateSubjectDto request
    ) {
        return Map.of(
                "updated",
                subjectService.updateSubjectName(apiKey, subject, request.getSubject())
        );
    }

    @WriteEndpoint
    @DeleteMapping("/{embeddingId}")
    public FaceResponseDto deleteFaceById(
            @PathVariable final UUID embeddingId,
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return subjectService.removeSubjectEmbedding(apiKey, embeddingId)
                .map(embedding -> new FaceResponseDto(embeddingId.toString(), embedding.getSubject().getSubjectName()))
                .orElse(new FaceResponseDto());
    }

    @PostMapping(value = "/{embeddingId}/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<FaceVerification>> recognizeFile(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true) @RequestParam final MultipartFile file,
            @ApiParam(value = LIMIT_DESC) @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false) @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC) @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC) @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status
    ) {
        imageValidator.validate(file);
        ProcessImageParams processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of(IMAGE_ID, embeddingId))
                .apiKey(apiKey)
                .detProbThreshold(detProbThreshold)
                .file(file)
                .facePlugins(facePlugins)
                .limit(limit)
                .status(status)
                .build();

        return subjectService.verifyFace(processImageParams);
    }

    @PostMapping(value = "/{embeddingId}/verify",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FaceVerification>> recognizeBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId,
            @RequestBody @Valid VerifyRequest verifyRequest
    ) {
        imageValidator.validateBase64(verifyRequest.getImageAsBase64());
        ProcessImageParams processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of(IMAGE_ID, embeddingId))
                .apiKey(apiKey)
                .detProbThreshold(verifyRequest.getDetProbThreshold())
                .imageBase64(verifyRequest.getImageAsBase64())
                .facePlugins(verifyRequest.getFacePlugins())
                .limit(verifyRequest.getLimit())
                .status(verifyRequest.getStatus())
                .build();

        return subjectService.verifyFace(processImageParams);
    }
}
