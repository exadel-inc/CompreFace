package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectRepositoryTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    SubjectRepository subjectRepository;

    @Test
    void testFindByApiKey() {
        var model = dbHelper.insertModel();
        var subject1 = dbHelper.insertSubject(model, "subject1");
        var subject2 = dbHelper.insertSubject(model, "subject2");

        assertThat(subjectRepository.findByApiKey(model.getApiKey())).contains(subject1, subject2);
    }

    @Test
    void testGetSubjectNames() {
        var model = dbHelper.insertModel();
        var subject1 = dbHelper.insertSubject(model, "subject1");
        var subject2 = dbHelper.insertSubject(model, "subject2");

        assertThat(subjectRepository.getSubjectNames(model.getApiKey())).contains(subject1.getSubjectName(), subject2.getSubjectName());
    }

    @Test
    void testFindByApiKeyAndSubjectNameIgnoreCase() {
        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subJect");

        final List<String> list = List.of("subJECT", "SUBJECT", "subject", "subJect");
        for (String variant : list) {
            final Optional<Subject> subjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(model.getApiKey(), variant);
            assertThat(subjectOptional).isPresent();

            final Subject dbSubject = subjectOptional.get();
            assertThat(dbSubject.getId()).isEqualTo(subject.getId());
        }

        // wrong api_key
        assertThat(subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(UUID.randomUUID().toString(), "subJect")).isEmpty();
    }

    @Test
    @Transactional
    void testDeleteByApiKey() {
        var model = dbHelper.insertModel();
        int count = 3;
        for (int i = 0; i < count; i++) {
            dbHelper.insertSubject(model, "subject" + i);
        }

        assertThat(subjectRepository.deleteByApiKey(model.getApiKey())).isEqualTo(count);
    }
}