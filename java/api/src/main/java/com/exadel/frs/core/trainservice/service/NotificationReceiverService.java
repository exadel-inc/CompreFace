package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;

@Service("notificationReceiverService")
@Slf4j
@RequiredArgsConstructor
public class NotificationReceiverService {

    private static final TypeReference<CacheActionDto<Map<String, Object>>> CACHE_ACTION_DTO_TR = new TypeReference<>() {};
    @Qualifier("dsPgNot")
    private final PGDataSource pgNotificationDatasource;
    private PGConnection connection;
    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper;
    private static PGNotificationListener listener;

    @PostConstruct
    public void setUpNotification() {

        listener = new PGNotificationListener() {

            @Override
            public void notification(int processId, String channelName, String payload) {
                log.info(String.format("/channels3/ channel name: %1$s payload %2$s", channelName, payload));
                if (channelName.equals("face_collection_update_msg")) {
                    synchronizeCacheWithNotification(payload);
                }
            }

            @Override
            public void closed() {
                log.info("face_collection_update_msg closed");
            }
        };
        try {
            connection = pgNotificationDatasource.getConnection().unwrap(PGConnection.class);

            Statement statement = connection.createStatement();
            statement.executeUpdate("LISTEN face_collection_update_msg");

            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connection.addNotificationListener(listener);
    }

    // package private for test purposes
    void synchronizeCacheWithNotification(String payload) {
        try {
            var cacheActionDto = objectMapper.readValue(payload, CACHE_ACTION_DTO_TR);
            if (SERVER_UUID.equals(cacheActionDto.getServerUUID())) {
                return;
            }
            if (Objects.isNull(cacheActionDto.getServerUUID())) {
                log.warn("Received notification with empty serverUUID: {}", cacheActionDto);
                return;
            }
            if (StringUtils.isBlank(cacheActionDto.getApiKey())) {
                log.warn("Received notification with blank api key: {}", cacheActionDto);
                return;
            }
            if (Objects.isNull(cacheActionDto.getCacheAction())) {
                log.warn("Received notification with blank cache action type: {}", cacheActionDto);
                return;
            }
            processNotification(cacheActionDto);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    private void processNotification(CacheActionDto<Map<String, Object>> action) {
        switch (action.getCacheAction()) {
            case INVALIDATE:
                notificationHandler.invalidate(action);
                break;
            case REMOVE_SUBJECTS:
                notificationHandler.removeSubjects(convert(action, new TypeReference<>() {}));
                break;
            case ADD_EMBEDDINGS:
                notificationHandler.addEmbeddings(convert(action, new TypeReference<>() {}));
                break;
            case REMOVE_EMBEDDINGS:
                notificationHandler.removeEmbeddings(convert(action, new TypeReference<>() {}));
                break;
            case RENAME_SUBJECTS:
                notificationHandler.renameSubjects(convert(action, new TypeReference<>() {}));
                break;
            case DELETE:
                notificationHandler.handleDelete(action);
                break;
            case UPDATE:
                notificationHandler.handleUpdate(action);
                break;
            default:
                log.error("Can't process action with actionType={}", action.getCacheAction());
                break;
        }
    }

    @SneakyThrows
    private <T> CacheActionDto<T> convert(CacheActionDto<Map<String, Object>> action, TypeReference<T> tf) {
        return action.withPayload(
            objectMapper.convertValue(action.getPayload(), tf)
        );
    }
}
