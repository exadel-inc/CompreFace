package com.exadel.frs.core.trainservice;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeApp;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEmbedding;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeImg;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeModel;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeSubject;
import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.repository.AppRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbHelper {

    @Autowired
    AppRepository appRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    ModelStatisticRepository modelStatisticRepository;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Autowired
    ImgRepository imgRepository;

    public Model insertModel() {
        return insertModel(ModelType.RECOGNITION);
    }

    public Model insertModel(ModelType type) {
        var apiKey = UUID.randomUUID().toString();
        var app = appRepository.save(makeApp(apiKey));
        return modelRepository.save(makeModel(apiKey, type, app));
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

    public ModelStatistic insertModelStatistic(Model model, int requestCount, final LocalDateTime createDate) {
        var statistic = ModelStatistic.builder()
                                      .createdDate(createDate)
                                      .requestCount(requestCount)
                                      .model(model)
                                      .build();
        return modelStatisticRepository.save(statistic);
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