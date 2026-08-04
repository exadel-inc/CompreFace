package com.exadel.frs.service;

import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Removes embeddings and their images before a model or an app row is deleted.
 *
 * Deleting a model cascades down to subject and embedding inside the database, but
 * embedding.img_id has no ON DELETE action, so the images would survive with nothing
 * referencing them. A cascade runs no application code, so the cleanup in
 * {@code SubjectDao} on the api side never gets a chance to run for these two paths.
 *
 * Shared by ModelService and AppService: ModelService already depends on AppService, so
 * neither can call the other without creating a cycle.
 */
@Service
@RequiredArgsConstructor
public class FaceDataCleaner {

    private final EmbeddingRepository embeddingRepository;
    private final ImgRepository imgRepository;

    /**
     * Deletes every embedding belonging to the given service api key, then the images
     * left without a reference. Must run before the model row is deleted, otherwise the
     * cascade removes the embeddings first and the images can no longer be traced.
     */
    public void deleteFaceDataByApiKey(final String apiKey) {
        final List<UUID> imgIds = imgRepository.findImgIdsBySubjectApiKey(apiKey);

        embeddingRepository.deleteBySubjectApiKey(apiKey);
        deleteOrphanImgs(imgIds);
    }

    public void deleteFaceDataByApiKeys(final Collection<String> apiKeys) {
        apiKeys.forEach(this::deleteFaceDataByApiKey);
    }

    private void deleteOrphanImgs(final Collection<UUID> imgIds) {
        for (UUID imgId : imgIds) {
            if (imgId != null && imgRepository.countRelatedEmbeddings(imgId) == 0) {
                imgRepository.deleteById(imgId);
            }
        }
    }
}
