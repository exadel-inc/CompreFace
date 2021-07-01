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
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.service.NotificationSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEmbedding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingCacheProviderTest {

    private static final String API_KEY = "model_key";

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private NotificationSenderService notificationSenderService;

    @InjectMocks
    private EmbeddingCacheProvider embeddingCacheProvider;

    @Test
    void getOrLoad() {
        var embeddings = new Embedding[]{
                makeEmbedding("A", API_KEY),
                makeEmbedding("B", API_KEY),
                makeEmbedding("C", API_KEY)
        };

        when(embeddingService.doWithEmbeddingsStream(eq(API_KEY), any()))
                .thenAnswer(invocation -> {
                    var function = (Function<Stream<Embedding>, ?>) invocation.getArgument(1);
                    return function.apply(Stream.of(embeddings));
                });

        var actual = embeddingCacheProvider.getOrLoad(API_KEY);

        assertThat(actual, notNullValue());
        assertThat(actual.getProjections(), notNullValue());
        assertThat(actual.getProjections().size(), is(embeddings.length));
        assertThat(actual.getEmbeddings(), notNullValue());
    }
}
