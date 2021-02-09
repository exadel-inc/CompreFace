package com.exadel.frs.core.trainservice;

import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.enums.ModelType;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class ItemsBuilder {

    private static final double EMBEDDING = 100500;

    public static App makeApp(long id, String apiKey) {
        return App.builder()
                .name("App" + System.currentTimeMillis())
                .guid(UUID.randomUUID().toString())
                .apiKey(apiKey)
                .id(id)
                .build();
    }

    public static Model makeModel(String apiKey, ModelType type, App app) {
        return Model.builder()
                .apiKey(apiKey)
                .name("Model" + UUID.randomUUID())
                .type(type)
                .guid(UUID.randomUUID().toString())
                .app(app)
                .build();
    }

    public static Face makeFace(final String name, final String modelApiKey) {
        return new Face()
                .setFaceName(name)
                .setApiKey(modelApiKey)
                .setEmbedding(new Face.Embedding(List.of(EMBEDDING), null))
                .setId(randomUUID().toString());
    }

}
