package com.exadel.frs.core.trainservice.component.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectMigrationService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void doFaceMigrationInTransaction(String apiKey, String faceId, String faceName) {
        // try to find existing subject by {api_key, ignore_case<subject_name>} pair
        String subjectId = jdbcTemplate.query(
                "select id from subject where api_key = ? and upper(subject_name) = upper(?)",
                new Object[]{apiKey, faceName},
                rs -> rs.next() ? rs.getString("id") : null
        );

        if (subjectId == null) {
            // no existing subject => we should insert
            jdbcTemplate.update("insert into subject(id, api_key, subject_name) values(?, ?, ?)", faceId, apiKey, faceName);
            subjectId = faceId;

            log.debug("Inserted subject with id {}", subjectId);
        }

        // migrate image
        jdbcTemplate.update(
                "insert into img(subject_id, raw_img_fs, embeddings) select ?, i.raw_img_fs, f.embeddings from face f inner join image i on i.face_id = f.id where f.id = ?",
                subjectId, faceId
        );

        // mark as migrated
        jdbcTemplate.update("update face set migrated = ? where id = ?", true, faceId);

        log.debug("Inserted image for subject {}", subjectId);
    }
}
