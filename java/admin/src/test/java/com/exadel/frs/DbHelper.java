package com.exadel.frs;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.ResetPasswordToken;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ResetPasswordTokenRepository;
import com.exadel.frs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.exadel.frs.ItemsBuilder.*;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;

@Service
// TODO think about common helper for admin/core
public class DbHelper {

    @Value("${forgot-password.reset-password-token.expires}")
    private long resetPasswordTokenExpires;

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

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    ResetPasswordTokenRepository resetPasswordTokenRepository;

    public Model insertModel() {
        final String apiKey = randomUUID().toString();

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

    public User insertUser(String email) {
        var userCreateDto = UserCreateDto.builder()
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .password("password")
                                         .email(email)
                                         .build();
        var user = userService.createUser(userCreateDto);
        user.setRegistrationToken(null);
        user.setEnabled(true);
        return userRepository.saveAndFlush(user);
    }

    public ResetPasswordToken insertResetPasswordToken(User user) {
        var token = ResetPasswordToken.builder()
                                      .token(randomUUID())
                                      .expireIn(now(UTC).plus(resetPasswordTokenExpires, MILLIS))
                                      .user(user)
                                      .build();
        return resetPasswordTokenRepository.saveAndFlush(token);
    }
}
