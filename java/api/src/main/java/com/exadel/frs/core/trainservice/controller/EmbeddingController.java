package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.commonservice.system.global.Constants.DET_PROB_THRESHOLD;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_KEY_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.CACHE_CONTROL_HEADER_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.DET_PROB_THRESHOLD_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.FACE_PLUGINS;
import static com.exadel.frs.core.trainservice.system.global.Constants.FACE_PLUGINS_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_ID;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_IDS_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_ID_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_WITH_ONE_FACE_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_DEFAULT_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_MIN_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.NUMBER_VALUE_EXAMPLE;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS_DEFAULT_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.SUBJECT;
import static com.exadel.frs.core.trainservice.system.global.Constants.SUBJECT_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.SUBJECT_NAME_IS_EMPTY;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.springframework.http.HttpStatus.CREATED;
import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.EmbeddingDto;
import com.exadel.frs.core.trainservice.dto.EmbeddingsRecognitionRequest;
import com.exadel.frs.core.trainservice.dto.EmbeddingsVerificationProcessResponse;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerificationResult;
import com.exadel.frs.core.trainservice.mapper.EmbeddingMapper;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.service.SubjectService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping(API_V1 + "/recognition")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final SubjectService subjectService;
    private final ImageExtensionValidator imageValidator;
    private final EmbeddingMapper embeddingMapper;
    private final FacesMapper facesMapper;

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping("/faces")
    public EmbeddingDto addEmbedding(
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = SUBJECT_DESC, required = true)
            @Valid
            @NotBlank(message = SUBJECT_NAME_IS_EMPTY)
            @RequestParam(SUBJECT)
            final String subjectName,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
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
    @PostMapping(value = "/faces", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmbeddingDto addEmbeddingBase64(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = SUBJECT_DESC)
            @Valid
            @NotBlank(message = SUBJECT_NAME_IS_EMPTY)
            @RequestParam(value = SUBJECT)
            final String subjectName,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @Valid
            @RequestBody
            final Base64File request
    ) {
        imageValidator.validateBase64(request.getContent());

        final Pair<Subject, Embedding> pair = subjectService.saveCalculatedEmbedding(
                request.getContent(),
                subjectName,
                detProbThreshold,
                apiKey
        );

        return new EmbeddingDto(pair.getRight().getId().toString(), subjectName);
    }

    @ResponseBody
    @GetMapping(value = "/faces/{embeddingId}/img", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadImg(HttpServletResponse response,
                              @ApiParam(value = API_KEY_DESC, required = true)
                              @RequestHeader(name = X_FRS_API_KEY_HEADER)
                              final String apiKey,
                              @ApiParam(value = IMAGE_ID_DESC, required = true)
                              @PathVariable
                              final UUID embeddingId
    ) {
        response.addHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER_VALUE);
        return embeddingService.getImg(apiKey, embeddingId)
                               .map(Img::getContent)
                               .orElse(new byte[]{});
    }

    @GetMapping("/faces")
    public Faces listEmbeddings(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = SUBJECT_DESC)
            @Valid
            @RequestParam(name = SUBJECT, required = false)
            final String subjectName,
            final Pageable pageable
    ) {
        return new Faces(embeddingService.listEmbeddings(apiKey, subjectName, pageable).map(embeddingMapper::toResponseDto));
    }

    @WriteEndpoint
    @DeleteMapping("/faces")
    public Map<String, Object> removeAllSubjectEmbeddings(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = SUBJECT_DESC)
            @Validated
            @NotBlank(message = SUBJECT_NAME_IS_EMPTY)
            @RequestParam(name = SUBJECT, required = false)
            final String subjectName
    ) {
        return Map.of(
                "deleted",
                subjectService.removeAllSubjectEmbeddings(apiKey, subjectName)
        );
    }

    @WriteEndpoint
    @DeleteMapping("/faces/{embeddingId}")
    public EmbeddingDto deleteEmbeddingById(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true)
            @PathVariable
            final UUID embeddingId
    ) {
        var embedding = subjectService.removeSubjectEmbedding(apiKey, embeddingId);
        return new EmbeddingDto(embeddingId.toString(), embedding.getSubject().getSubjectName());
    }

    @WriteEndpoint
    @PostMapping("/faces/delete")
    public List<EmbeddingDto> deleteEmbeddingsById(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = IMAGE_IDS_DESC, required = true)
            @RequestBody
            final List<UUID> embeddingIds
    ) {
        List<Embedding> list = subjectService.removeSubjectEmbeddings(apiKey, embeddingIds);
        List<EmbeddingDto> dtoList = list.stream()
                                         .map(c -> new EmbeddingDto(c.getId().toString(), c.getSubject().getSubjectName()))
                                         .collect(Collectors.toList());
        return dtoList;
    }

    @PostMapping(value = "/faces/{embeddingId}/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VerificationResult recognizeFile(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = LIMIT_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false)
            @Min(value = 0, message = LIMIT_MIN_DESC)
            final Integer limit,
            @ApiParam(value = IMAGE_ID_DESC, required = true)
            @PathVariable
            final UUID embeddingId,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC)
            @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "")
            final String facePlugins,
            @ApiParam(value = STATUS_DESC)
            @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE)
            final Boolean status
    ) {
        imageValidator.validate(file);
        var processImageParams = ProcessImageParams.builder()
                                                   .additionalParams(Map.of(IMAGE_ID, embeddingId))
                                                   .apiKey(apiKey)
                                                   .detProbThreshold(detProbThreshold)
                                                   .file(file)
                                                   .facePlugins(facePlugins)
                                                   .limit(limit)
                                                   .status(status)
                                                   .build();

        var pair = subjectService.verifyFace(processImageParams);
        return new VerificationResult(
                pair.getLeft(),
                facesMapper.toPluginVersionsDto(pair.getRight())
        );
    }

    @PostMapping(value = "/faces/{embeddingId}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VerificationResult recognizeBase64(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true)
            @PathVariable
            final UUID embeddingId,
            @ApiParam(value = LIMIT_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false)
            @Min(value = 0, message = LIMIT_MIN_DESC)
            final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC)
            @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "")
            final String facePlugins,
            @ApiParam(value = STATUS_DESC)
            @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE)
            final Boolean status,
            @RequestBody
            @Valid
            final Base64File request
    ) {
        imageValidator.validateBase64(request.getContent());

        var processImageParams = ProcessImageParams.builder()
                                                   .additionalParams(Map.of(IMAGE_ID, embeddingId))
                                                   .apiKey(apiKey)
                                                   .detProbThreshold(detProbThreshold)
                                                   .imageBase64(request.getContent())
                                                   .facePlugins(facePlugins)
                                                   .limit(limit)
                                                   .status(status)
                                                   .build();

        var pair = subjectService.verifyFace(processImageParams);
        return new VerificationResult(
                pair.getLeft(),
                facesMapper.toPluginVersionsDto(pair.getRight())
        );
    }

    @PostMapping(value = "/embeddings/faces/{imageId}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmbeddingsVerificationProcessResponse recognizeEmbeddings(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true)
            @PathVariable
            final UUID imageId,
            @RequestBody
            @Valid
            final EmbeddingsRecognitionRequest recognitionRequest
    ) {
        ProcessEmbeddingsParams processParams =
                ProcessEmbeddingsParams.builder()
                                       .apiKey(apiKey)
                                       .embeddings(recognitionRequest.getEmbeddings())
                                       .additionalParams(Map.of(IMAGE_ID, imageId))
                                       .build();

        return subjectService.verifyEmbedding(processParams);
    }

    @RequiredArgsConstructor
    private static final class Faces {

        private final Page<EmbeddingDto> source;

        // As of backward compatibility we are not allowed to rename property 'faces' --> 'embedding'
        public List<EmbeddingDto> getFaces() {
            return source.getContent();
        }

        @JsonProperty("total_pages")
        public int getTotalPages() {
            return source.getTotalPages();
        }

        @JsonProperty("total_elements")
        public long getTotalElements() {
            return source.getTotalElements();
        }

        @JsonProperty("page_number")
        public int getNumber() {
            return source.getNumber();
        }

        @JsonProperty("page_size")
        public int getSize() {
            return source.getSize();
        }
    }
}
