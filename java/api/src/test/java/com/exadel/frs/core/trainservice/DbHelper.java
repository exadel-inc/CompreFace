package com.exadel.frs.core.trainservice;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.repository.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.exadel.frs.core.trainservice.ItemsBuilder.*;

@Service
public class DbHelper {

    @Autowired
    AppRepository appRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Autowired
    ImgRepository imgRepository;

    public Model insertModel() {
        final String apiKey = UUID.randomUUID().toString();

        var app = appRepository.save(makeApp(apiKey));
        return modelRepository.save(makeModel(apiKey, ModelType.RECOGNITION, app));
    }

    public Subject insertSubject(Model model, String subjectName) {
        return insertSubject(model.getApiKey(), subjectName);
    }

    /**
     * Current method assumes, that model with such api_key already exists
     *
     * @param apiKey      existing api key (app and model already in DB)
     * @param subjectName subject name
     * @return subject object
     */
    public Subject insertSubject(String apiKey, String subjectName) {
        return subjectRepository.save(makeSubject(apiKey, subjectName));
    }

    /**
     * Method inserts new app/model before subject insert.
     *
     * @param subjectName subject name
     * @return subject object
     */
    public Subject insertSubject(String subjectName) {
        var model = insertModel();
        return insertSubject(model.getApiKey(), subjectName);
    }

    public Embedding insertEmbeddingNoImg(Subject subject) {
        return insertEmbeddingNoImg(subject, null);
    }

    public Embedding insertEmbeddingNoImg(Subject subject, String calculator) {
        return insertEmbeddingNoImg(subject, calculator, null);
    }

    public Embedding insertEmbeddingNoImg(Subject subject, String calculator, double[] embedding) {
        return embeddingRepository.save(makeEmbedding(subject, calculator, embedding, null));
    }

    public Embedding insertEmbeddingWithImg(Subject subject) {
        return insertEmbeddingWithImg(subject, null, null);
    }

    public Embedding insertEmbeddingWithImg(Subject subject, String calculator) {
        return insertEmbeddingWithImg(subject, calculator, null);
    }

    public Embedding insertEmbeddingWithImg(Subject subject, String calculator, double[] embedding) {
        var img = insertImg();
        return insertEmbeddingWithImg(subject, calculator, embedding, img);
    }

    public Embedding insertEmbeddingWithImg(Subject subject, String calculator, double[] embedding, Img img) {
        return embeddingRepository.save(makeEmbedding(subject, calculator, embedding, img));
    }

    public Img insertImg() {
        return imgRepository.save(makeImg());
    }
}