package com.exadel.frs.commonservice.system.liquibase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class FacesToSubjectMigrationProcessor {

    private final TransactionalFaceMigration transactionalFaceMigration;
    private final JdbcTemplate jdbcTemplate;

    public int start() {
        final String sql = "select " +
                "   f.id as face_id, " +
                "   f.face_name, " +
                "   f.api_key, " +
                "   case when raw_img_fs is null then false else true end as has_image " +
                "from " +
                "   face f inner join image i on f.id = i.face_id " +
                "where " +
                "   migrated = ?";


        // just as wrapper to bypass immutable variables inside closure
        final var counter = new AtomicInteger(0);

        jdbcTemplate.query(sql, new Object[]{false}, rs -> {
            final var apiKey = rs.getString("api_key");
            final var faceId = rs.getString("face_id");
            final var faceName = rs.getString("face_name");
            final var hasImage = rs.getBoolean("has_image");

            transactionalFaceMigration.doFaceMigrationInTransaction(apiKey, faceId, faceName, hasImage);
            counter.incrementAndGet();

            log.debug("{} face(s) done", counter.get());
        });

        return counter.get();
    }
}
