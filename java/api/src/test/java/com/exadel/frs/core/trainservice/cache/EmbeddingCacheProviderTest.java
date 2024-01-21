/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.projection.EmbeddingProjection;
import com.exadel.frs.commonservice.projection.EnhancedEmbeddingProjection;
import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.AddEmbeddings;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.CacheAction;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RemoveEmbeddings;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RemoveSubjects;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RenameSubjects;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.service.NotificationReceiverService;
import com.exadel.frs.core.trainservice.service.NotificationSenderService;
import com.exadel.frs.core.trainservice.system.global.Constants;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEnhancedEmbeddingProjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingCacheProviderTest {

    private static final String API_KEY = "model_key";
    private static final String SUBJECT_NAME = "subject_name";
    private static final String NEW_SUBJECT_NAME = "new_subject_name";
    private static final UUID EMBEDDING_ID_1 = UUID.randomUUID();
    private static final UUID EMBEDDING_ID_2 = UUID.randomUUID();
    private static final String TEST_CALCULATOR = "test-calculator";

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private NotificationSenderService notificationSenderService;

    @Mock
    private NotificationReceiverService notificationReceiverService;

    @InjectMocks
    private EmbeddingCacheProvider embeddingCacheProvider;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetStaticCache() {
        embeddingCacheProvider.invalidate(API_KEY);
        when(embeddingService.doWithEnhancedEmbeddingProjectionStream(eq(API_KEY), any()))
            .thenAnswer(invocation -> {
                var function = (Function<Stream<EnhancedEmbeddingProjection>, ?>) invocation.getArgument(1);
                return function.apply(Stream.of());
            });
        embeddingCacheProvider.getOrLoad(API_KEY);
        reset(embeddingService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrLoad() {
        embeddingCacheProvider.invalidate(API_KEY);
        reset(embeddingService);
        var projections = new EnhancedEmbeddingProjection[]{
                makeEnhancedEmbeddingProjection("A"),
                makeEnhancedEmbeddingProjection("B"),
                makeEnhancedEmbeddingProjection("C")
        };

        when(embeddingService.doWithEnhancedEmbeddingProjectionStream(eq(API_KEY), any()))
                .thenAnswer(invocation -> {
                    var function = (Function<Stream<EnhancedEmbeddingProjection>, ?>) invocation.getArgument(1);
                    return function.apply(Stream.of(projections));
                });

        var actual = embeddingCacheProvider.getOrLoad(API_KEY);

        verify(embeddingService, times(1)).doWithEnhancedEmbeddingProjectionStream(eq(API_KEY), any());
        assertThat(actual, notNullValue());
        assertThat(actual.getProjections(), notNullValue());
        assertThat(actual.getProjections().size(), is(projections.length));
        assertThat(actual.getEmbeddings(), notNullValue());
    }

    @Test
    void removeEmbedding() {
        // arrange
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_1));
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));
        reset(notificationReceiverService);

        // act
        embeddingCacheProvider.removeEmbedding(API_KEY, new EmbeddingProjection(EMBEDDING_ID_1, SUBJECT_NAME));

        // assert
        var embeddings = embeddingCacheProvider.getOrLoad(API_KEY);
        Assertions.assertThat(embeddings.getEmbeddings().size(0)).isEqualTo(1);
        Assertions.assertThat(embeddings.getProjections()).containsOnly(new EmbeddingProjection(EMBEDDING_ID_2, SUBJECT_NAME));

        verify(notificationSenderService, times(1)).notifyCacheChange(
            buildCacheActionDto(CacheAction.REMOVE_EMBEDDINGS, new RemoveEmbeddings(Map.of(SUBJECT_NAME, List.of(EMBEDDING_ID_1))))
        );
    }

    @Test
    void updateSubjectName() {
        // arrange
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_1));
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));
        reset(notificationSenderService);

        // act
        embeddingCacheProvider.updateSubjectName(API_KEY, SUBJECT_NAME, NEW_SUBJECT_NAME);

        // assert
        var embeddings = embeddingCacheProvider.getOrLoad(API_KEY);
        Assertions.assertThat(embeddings.getEmbeddings().size(0)).isEqualTo(2);
        Assertions.assertThat(embeddings.getProjections()).containsExactly(
            new EmbeddingProjection(EMBEDDING_ID_1, NEW_SUBJECT_NAME),
            new EmbeddingProjection(EMBEDDING_ID_2, NEW_SUBJECT_NAME)
        );

        verify(notificationSenderService, times(1)).notifyCacheChange(
            buildCacheActionDto(CacheAction.RENAME_SUBJECTS, new RenameSubjects(Map.of(SUBJECT_NAME, NEW_SUBJECT_NAME)))
        );
    }

    @Test
    void removeBySubjectName() {
        // arrange
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_1));
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));
        reset(notificationSenderService);

        // act
        embeddingCacheProvider.removeBySubjectName(API_KEY, SUBJECT_NAME);

        // assert
        var embeddings = embeddingCacheProvider.getOrLoad(API_KEY);
        Assertions.assertThat(embeddings.getEmbeddings().size(0)).isZero();
        Assertions.assertThat(embeddings.getProjections()).isEmpty();

        verify(notificationSenderService, times(1)).notifyCacheChange(
            buildCacheActionDto(CacheAction.REMOVE_SUBJECTS, new RemoveSubjects(List.of(SUBJECT_NAME)))
        );
    }

    @Test
    void addEmbedding() {
        // act
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));

        // assert
        var embeddings = embeddingCacheProvider.getOrLoad(API_KEY);
        Assertions.assertThat(embeddings.getEmbeddings().size(0)).isEqualTo(1);
        Assertions.assertThat(embeddings.getProjections()).containsOnly(new EmbeddingProjection(EMBEDDING_ID_2, SUBJECT_NAME));

        verify(notificationSenderService, times(1)).notifyCacheChange(
            buildCacheActionDto(CacheAction.ADD_EMBEDDINGS, new AddEmbeddings(List.of(EMBEDDING_ID_2)))
        );
    }

    @Test
    void invalidate() {
        // arrange
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));
        reset(notificationSenderService);

        // act
        embeddingCacheProvider.invalidate(API_KEY);

        // assert
        embeddingCacheProvider.expose(API_KEY, ec -> {
            Assertions.assertThat(ec.getEmbeddings().size(0)).isZero();
            Assertions.assertThat(ec.getProjections()).isEmpty();
        });
        verify(notificationSenderService).notifyCacheChange(
            buildCacheActionDto(
                CacheAction.INVALIDATE,
                null
            )
        );
    }

    @Test
    void receivePutOnCache() {
        // arrange
        receiveInvalidateCache();
    }

    @Test
    void receiveInvalidateCache() {
        // arrange
        embeddingCacheProvider.addEmbedding(API_KEY, buildEmbedding(EMBEDDING_ID_2));

        // act
        embeddingCacheProvider.receiveInvalidateCache(API_KEY);

        // assert
        embeddingCacheProvider.expose(API_KEY, ec -> {
            Assertions.assertThat(ec.getEmbeddings().size(0)).isZero();
            Assertions.assertThat(ec.getProjections()).isEmpty();
        });
    }

    private static <T> CacheActionDto<T> buildCacheActionDto(
        CacheAction cacheAction,
        T payload
    ) {
        return new CacheActionDto<>(
            cacheAction,
            API_KEY,
            Constants.SERVER_UUID,
            payload
        );
    }

    static Embedding buildEmbedding(
        UUID embeddingId
    ) {
        var subj = new Subject(
            UUID.randomUUID(),
            API_KEY,
            SUBJECT_NAME
        );
        return new Embedding(
            embeddingId,
            subj,
            new double[]{21.22, 222.444},
            TEST_CALCULATOR,
            null
        );
    }
}
