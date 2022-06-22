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

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEmbedding;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEnhancedEmbeddingProjection;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.EnhancedEmbeddingProjection;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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
        var projection1 = makeEnhancedEmbeddingProjection("A");
        var projection2 = makeEnhancedEmbeddingProjection("B");
        var projection3 = makeEnhancedEmbeddingProjection("C");
        var projections = new EnhancedEmbeddingProjection[]{projection1, projection2, projection3};
        var embeddingCollection = EmbeddingCollection.from(Stream.of(projections));

        assertThat(embeddingCollection).isNotNull();
        assertThat(embeddingCollection.getIndexMap()).isNotNull();
        assertThat(embeddingCollection.getIndexMap()).hasSize(projections.length);

        assertThat(embeddingCollection.getIndexMap()).containsEntry(0, EmbeddingProjection.from(projection1));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(1, EmbeddingProjection.from(projection2));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(2, EmbeddingProjection.from(projection3));
    }

    @Test
    void testAdd() {
        var projections = new EnhancedEmbeddingProjection[]{
                makeEnhancedEmbeddingProjection("A"),
                makeEnhancedEmbeddingProjection("B"),
                makeEnhancedEmbeddingProjection("C")
        };
        var embeddingCollection = EmbeddingCollection.from(Stream.of(projections));
        var newEmbedding = makeEmbedding("D", API_KEY);

        var key = embeddingCollection.addEmbedding(newEmbedding);
        assertThat(key).isNotNull();

        assertThat(embeddingCollection.getProjections()).hasSize(projections.length + 1);
        assertThat(embeddingCollection.getIndexMap()).containsEntry(projections.length, EmbeddingProjection.from(newEmbedding));
    }

    @Test
    void testRemove() {
        var projection1 = makeEnhancedEmbeddingProjection("A");
        var projection2 = makeEnhancedEmbeddingProjection("B");
        var projection3 = makeEnhancedEmbeddingProjection("C");
        var projections = new EnhancedEmbeddingProjection[]{projection1, projection2, projection3};
        var embeddingCollection = EmbeddingCollection.from(Stream.of(projections));

        embeddingCollection.removeEmbedding(EmbeddingProjection.from(projection1));

        assertThat(embeddingCollection.getProjections()).hasSize(projections.length - 1);
        assertThat(embeddingCollection.getIndexMap()).containsEntry(0, EmbeddingProjection.from(projection2));
        assertThat(embeddingCollection.getIndexMap()).containsEntry(1, EmbeddingProjection.from(projection3));
    }
}