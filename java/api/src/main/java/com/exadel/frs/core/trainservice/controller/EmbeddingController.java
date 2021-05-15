package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.EmbeddingDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.EmbeddingMapper;
import com.exadel.frs.core.trainservice.service.SubjectService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.exadel.frs.commonservice.system.global.Constants.DET_PROB_THRESHOLD;
import static com.exadel.frs.core.trainservice.system.global.Constants.*;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(API_V1 + "/recognition/faces")
@RequiredArgsConstructor
public class EmbeddingController {

    private final SubjectService subjectService;
    private final ImageExtensionValidator imageValidator;
    private final EmbeddingMapper embeddingMapper;

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping
    public EmbeddingDto addEmbedding(
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true) @RequestParam final MultipartFile file,
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(SUBJECT) final String subjectName,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey
    ) throws IOException {
        imageValidator.validate(file);

        final Pair<Subject, Embedding> pair = subjectService.saveCalculatedEmbedding(
                file,
                subjectName,
                detProbThreshold,
                apiKey
        );

        return new EmbeddingDto(pair.getRight().getId().toString(), subjectName);
    }

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmbeddingDto addEmbeddingBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = SUBJECT_DESC) @RequestParam(value = SUBJECT) String subjectName,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @Valid @RequestBody Base64File request) {
        imageValidator.validateBase64(request.getContent());

        final Pair<Subject, Embedding> pair = subjectService.saveCalculatedEmbedding(
                request.getContent(),
                subjectName,
                detProbThreshold,
                apiKey
        );

        return new EmbeddingDto(pair.getRight().getId().toString(), subjectName);
    }

    @GetMapping(value = "/{embeddingId}/img", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] downloadImg(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId) {
        return subjectService.getImg(apiKey, embeddingId)
                .map(Img::getContent)
                .orElse(new byte[]{});
    }

    @GetMapping
    public Page<EmbeddingDto> listEmbeddings(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            Pageable pageable) {
        return subjectService.listEmbeddings(apiKey, pageable)
                .map(embeddingMapper::toResponseDto);
    }

    @WriteEndpoint
    @DeleteMapping
    public Map<String, Object> removeAllSubjectEmbeddings(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(name = SUBJECT) @NotEmpty final String subjectName
    ) {
        return Map.of(
                "deleted",
                subjectService.removeAllSubjectEmbeddings(apiKey, subjectName)
        );
    }

    @WriteEndpoint
    @DeleteMapping("/{embeddingId}")
    public EmbeddingDto deleteEmbeddingById(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId) {
        return subjectService.removeSubjectEmbedding(apiKey, embeddingId)
                .map(embedding -> new EmbeddingDto(embeddingId.toString(), embedding.getSubject().getSubjectName()))
                .orElse(new EmbeddingDto());
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

    @PostMapping(value = "/{embeddingId}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FaceVerification>> recognizeBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId,
            @ApiParam(value = LIMIT_DESC) @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false) @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC) @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC) @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status,
            @RequestBody @Valid Base64File request
    ) {
        imageValidator.validateBase64(request.getContent());

        ProcessImageParams processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of(IMAGE_ID, embeddingId))
                .apiKey(apiKey)
                .detProbThreshold(detProbThreshold)
                .imageBase64(request.getContent())
                .facePlugins(facePlugins)
                .limit(limit)
                .status(status)
                .build();

        return subjectService.verifyFace(processImageParams);
    }
}
