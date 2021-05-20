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
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEmbedding;
import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingCollectionTest {

    private static final String API_KEY = "api_key";

    @Test
    void testRemoveFromEmpty() {
        var embeddingCollection = EmbeddingCollection.from(Stream.of());
        final EmbeddingProjection removed = embeddingCollection.removeEmbedding(new EmbeddingProjection(UUID.randomUUID(), "subject_name"));

        assertThat(removed).isNull();
    }

    @Test
    void testAddToEmpty() {
        var embeddingCollection = EmbeddingCollection.from(Stream.of());
        assertThat(embeddingCollection.getEmbeddings().isEmpty()).isTrue();
        assertThat(embeddingCollection.getIndexMap()).isEmpty();
        assertThat(embeddingCollection.getProjections()).isEmpty();

        embeddingCollection.addEmbedding(makeEmbedding("A", API_KEY));

        assertThat(embeddingCollection.getEmbeddings().isEmpty()).isFalse();
        assertThat(embeddingCollection.getProjections()).hasSize(1);
    }

    @Test
    void testCreate() {
        var embedding1 = makeEmbedding("A", API_KEY);
        var embedding2 = makeEmbedding("B", API_KEY);
        var embedding3 = makeEmbedding("C", API_KEY);
        var embeddings = new Embedding[]{embedding1, embedding2, embedding3};
        var embeddingCollection = EmbeddingCollection.from(Stream.of(embeddings));

        assertThat(embeddingCollection).isNotNull();
        assertThat(embeddingCollection.getIndexMap()).isNotNull();
        assertThat(embeddingCollection.getIndexMap()).hasSize(embeddings.length);

        assertThat(embeddingCollection.getIndexMap()).containsEntry(0, EmbeddingProjection.from(embedding1));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(1, EmbeddingProjection.from(embedding2));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(2, EmbeddingProjection.from(embedding3));
    }

    @Test
    void testAdd() {
        var embeddings = new Embedding[]{
                makeEmbedding("A", API_KEY),
                makeEmbedding("B", API_KEY),
                makeEmbedding("C", API_KEY)
        };
        var embeddingCollection = EmbeddingCollection.from(Stream.of(embeddings));
        var newEmbedding = makeEmbedding("D", API_KEY);

        var key = embeddingCollection.addEmbedding(newEmbedding);
        assertThat(key).isNotNull();

        assertThat(embeddingCollection.getProjections()).hasSize(embeddings.length + 1);
        assertThat(embeddingCollection.getIndexMap()).containsEntry(embeddings.length, EmbeddingProjection.from(newEmbedding));
    }

    @Test
    void testRemove() {
        var embedding1 = makeEmbedding("A", API_KEY);
        var embedding2 = makeEmbedding("B", API_KEY);
        var embedding3 = makeEmbedding("C", API_KEY);
        var embeddings = new Embedding[]{embedding1, embedding2, embedding3};
        var embeddingCollection = EmbeddingCollection.from(Stream.of(embeddings));

        embeddingCollection.removeEmbedding(embedding1);

        assertThat(embeddingCollection.getProjections()).hasSize(embeddings.length - 1);
        assertThat(embeddingCollection.getIndexMap()).containsEntry(0, EmbeddingProjection.from(embedding2));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(1, EmbeddingProjection.from(embedding3));
    }
}