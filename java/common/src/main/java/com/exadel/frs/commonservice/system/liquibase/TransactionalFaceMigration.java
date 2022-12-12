package com.exadel.frs.commonservice.system.liquibase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionalFaceMigration {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void doFaceMigrationInTransaction(String apiKey, String faceId, String faceName, boolean hasImage) {
        // try to find existing subject by {api_key, ignore_case<subject_name>} pair
        UUID subjectId = jdbcTemplate.query(
                "select id from subject where api_key = ? and upper(subject_name) = upper(?)",
                new Object[]{apiKey, faceName},
                rs -> rs.next() ? rs.getObject("id", UUID.class) : null
        );

        // OneToOne -> OneToMany migration
        if (subjectId == null) {
            // subject not exists => we should insert it
            subjectId = UUID.fromString(faceId);

            jdbcTemplate.update(
                    "insert into subject(id, api_key, subject_name) values(?, ?, ?)",
                    subjectId, apiKey, faceName
            );

            log.debug("Inserted subject with id {}", subjectId);
        }

        UUID imgId = null;
        if (Boolean.TRUE.equals(hasImage)) {
            imgId = UUID.randomUUID();
            // create image
            jdbcTemplate.update(
                    "insert into img(id, content) select ?, i.raw_img_fs from face f inner join image i on i.face_id = f.id where f.id = ?",
                    imgId, faceId
            );
        }

        // create embedding
        jdbcTemplate.update(
                "insert into embedding(id, subject_id, embedding, calculator, img_id) " +
                        "select " +
                        "?, ?, array(select json_array_elements_text(f.embeddings -> 'embeddings'))::float8[], f.embeddings ->> 'calculatorVersion', ? " +
                        "from face f " +
                        "where f.id = ?",
                UUID.randomUUID(), subjectId, imgId, faceId
        );

        // mark as migrated
        jdbcTemplate.update("update face set migrated = ? where id = ?", true, faceId);
    }
}
