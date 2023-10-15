package com.exadel.frs.service;

import static com.exadel.frs.commonservice.enums.GlobalRole.OWNER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import com.exadel.frs.DbHelper;
import com.exadel.frs.EmbeddedPostgreSQLTest;
import com.exadel.frs.commonservice.repository.InstallInfoRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.commonservice.system.feign.ApperyStatisticsClient;
import com.exadel.frs.commonservice.system.feign.StatisticsFacesEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StatisticServiceTestIT extends EmbeddedPostgreSQLTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private InstallInfoRepository installInfoRepository;

    @MockBean
    private ApperyStatisticsClient apperyClient;

    @Autowired
    private StatisticService statisticService;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
        modelRepository.deleteAll();

        userRepository.flush();
        modelRepository.flush();
    }

    @Test
    void recordStatistics_UserDoesNotExist_ShouldExit() {
        var model = dbHelper.insertModel();
        dbHelper.insertSubject(model, "Subject1");
        dbHelper.insertSubject(model, "Subject2");
        dbHelper.insertSubject(model, "Subject3");

        statisticService.recordStatistics();

        assertThat(userRepository.count()).isZero();
        assertThat(modelRepository.count()).isEqualTo(1);
        assertThat(subjectRepository.count()).isEqualTo(3);

        verifyNoInteractions(apperyClient);
    }

    @Test
    void recordStatistics_UserIsNotOwner_ShouldExit() {
        dbHelper.insertUser("john@gmail.com");

        var model = dbHelper.insertModel();
        dbHelper.insertSubject(model, "Subject1");
        dbHelper.insertSubject(model, "Subject2");
        dbHelper.insertSubject(model, "Subject3");

        statisticService.recordStatistics();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(modelRepository.count()).isEqualTo(1);
        assertThat(subjectRepository.count()).isEqualTo(3);

        verifyNoInteractions(apperyClient);
    }

    @Test
    void recordStatistics_InstallInfoDoesNotExist_ShouldExit() {
        installInfoRepository.deleteAll();
        installInfoRepository.flush();

        dbHelper.insertUser("john@gmail.com");

        var model = dbHelper.insertModel();
        dbHelper.insertSubject(model, "Subject1");
        dbHelper.insertSubject(model, "Subject2");
        dbHelper.insertSubject(model, "Subject3");

        statisticService.recordStatistics();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(modelRepository.count()).isEqualTo(1);
        assertThat(subjectRepository.count()).isEqualTo(3);
        assertThat(installInfoRepository.count()).isZero();

        verifyNoInteractions(apperyClient);
    }

    @Test
    void recordStatistics_ThereIsOneModelThatHasThreeSubjects_ShouldRecordStatisticWithRangeBetweenOneAndTen() {
        dbHelper.insertUser("john@gmail.com", OWNER);

        var model = dbHelper.insertModel();
        dbHelper.insertSubject(model, "Subject1");
        dbHelper.insertSubject(model, "Subject2");
        dbHelper.insertSubject(model, "Subject3");

        var installGuid = installInfoRepository.findTopByOrderByInstallGuid().getInstallGuid();

        statisticService.recordStatistics();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(modelRepository.count()).isEqualTo(1);
        assertThat(subjectRepository.count()).isEqualTo(3);

        var statistic = new StatisticsFacesEntity(installGuid, model.getGuid(), "1-10");

        verify(apperyClient).create("qwe1-rty2-uio3", statistic);
    }

    @Test
    void recordStatistics_SomethingWentWrongWhileSendingStatistics_ShouldThrowApperyServiceException() {
        dbHelper.insertUser("john@gmail.com", OWNER);

        var model = dbHelper.insertModel();
        dbHelper.insertSubject(model, "Subject1");
        dbHelper.insertSubject(model, "Subject2");
        dbHelper.insertSubject(model, "Subject3");

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(modelRepository.count()).isEqualTo(1);
        assertThat(subjectRepository.count()).isEqualTo(3);
    }
}
