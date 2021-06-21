package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.sql.Statement;

import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;

@Service("notificationReceiverService")
@Slf4j
@RequiredArgsConstructor
public class NotificationReceiverService {

//    @Qualifier("dsPgNot")
//    private final PGDataSource pgNotificationDatasource;


    private final PGConnection connection;
    private final EmbeddingCacheProvider embeddingCacheProvider;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void setUpNotification() {

        System.out.println("!!!!");
        PGNotificationListener listener = new PGNotificationListener() {

            @Override
            public void notification(int processId, String channelName, String payload) {
                log.info(String.format("/channels3/ channel name: %1$s payload %2$s", channelName, payload));
                System.out.println("!");
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
//            PGConnection connection = (PGConnection) pgNotificationDatasource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("LISTEN face_collection_update_msg");

            statement.close();
            connection.addNotificationListener(listener);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }

    }

    private void synchronizeCacheWithNotification(String payload) {
        System.out.println("222");
        try {
            CacheActionDto cacheActionDto = objectMapper.readValue(payload, CacheActionDto.class);
            if (cacheActionDto != null
                    && !StringUtils.isBlank(cacheActionDto.getServerUUID())
                    && !cacheActionDto.getServerUUID().equals(SERVER_UUID)
                    && !StringUtils.isBlank(cacheActionDto.getApiKey())
                    && !StringUtils.isBlank(cacheActionDto.getCacheAction())
            ) {

                if (cacheActionDto.getCacheAction().equals("UPDATE")) {
                    embeddingCacheProvider.receivePutOnCache(cacheActionDto.getApiKey());
                } else if (cacheActionDto.getCacheAction().equals("DELETE")) {
                    embeddingCacheProvider.receiveInvalidateCache(cacheActionDto.getApiKey());
                }
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
