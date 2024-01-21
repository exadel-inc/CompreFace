package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // here and below "ignoreUnknown = true" for backward compatibility
public class CacheActionDto<T> {
    private CacheAction cacheAction;
    private String apiKey;
    @JsonProperty("uuid")
    private UUID serverUUID;
    private T payload;

    public <S> CacheActionDto<S> withPayload(S newPayload) {
        return new CacheActionDto<>(
            cacheAction,
            apiKey,
            serverUUID,
            newPayload
        );
    }

    public enum CacheAction {
        // UPDATE and DELETE stays here to support rolling update
        @Deprecated
        UPDATE,
        @Deprecated
        DELETE,
        REMOVE_EMBEDDINGS,
        REMOVE_SUBJECTS,
        ADD_EMBEDDINGS,
        RENAME_SUBJECTS,
        INVALIDATE
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoveEmbeddings {
        private Map<String, List<UUID>> embeddings;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoveSubjects {
        private List<String> subjects;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddEmbeddings {
        private List<UUID> embeddings;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RenameSubjects {
        private Map<String, String> subjectsNamesMapping;
    }
}
