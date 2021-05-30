package com.exadel.frs;

import com.exadel.frs.commonservice.entity.*;
import com.exadel.frs.commonservice.enums.ModelType;

import java.util.UUID;

public class ItemsBuilder {

    public static App makeApp(String apiKey) {
        return App.builder()
                .name("App" + System.currentTimeMillis())
                .guid(UUID.randomUUID().toString())
                .apiKey(apiKey)
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

    public static Embedding makeEmbedding(String subjectName, String apiKey) {
        return makeEmbedding(
                makeSubject(apiKey, subjectName),
                makeImg()
        );
    }

    public static Embedding makeEmbedding(Subject subject, String calculator, double[] embedding, Img img) {
        return new Embedding()
                .setSubject(subject)
                .setEmbedding(embedding != null ? embedding : new double[]{1.1, 2.2, 3.3})
                .setCalculator(calculator != null ? calculator : "calc")
                .setImg(img);
    }

    public static Embedding makeEmbedding(Subject subject) {
        return makeEmbedding(subject, null);
    }

    public static Embedding makeEmbedding(Subject subject, Img img) {
        return makeEmbedding(subject, null, null, img);
    }

    public static Img makeImg(byte[] content) {
        return new Img()
                .setContent(content);
    }

    public static Img makeImg() {
        return makeImg(new byte[]{
                (byte) 0xCA,
                (byte) 0xFE,
                (byte) 0xBA,
                (byte) 0xBE
        });
    }

    public static Subject makeSubject(String apiKey, String subjectName) {
        return new Subject()
                .setApiKey(apiKey)
                .setSubjectName(subjectName);
    }
}
