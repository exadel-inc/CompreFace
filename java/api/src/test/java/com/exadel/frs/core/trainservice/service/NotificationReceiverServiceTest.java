package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.AddEmbeddings;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.CacheAction;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RemoveEmbeddings;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RemoveSubjects;
import com.exadel.frs.core.trainservice.dto.CacheActionDto.RenameSubjects;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationReceiverServiceTest {
    static final UUID SERVER_UUID = UUID.randomUUID();
    public static final String API_KEY = "API_KEY";
    public static final String SUBJECT_NAME_1 = "SUBJECT_NAME_1";
    public static final String SUBJECT_NAME_2 = "SUBJECT_NAME_2";
    @Spy
    NotificationHandler handler = new NotificationHandler(Mockito.mock(EmbeddingCacheProvider.class), Mockito.mock(SubjectService.class));
    @Mock
    PGDataSource pgNotificationDatasource;
    @Mock
    PGConnection connection;
    @Mock
    PGNotificationListener listener;
    @Spy
    ObjectMapper objectMapper;
    @InjectMocks
    NotificationReceiverService service;

    @ParameterizedTest
    @MethodSource("okNotifications")
    void synchronizeCacheWithNormalNotification(
        CacheActionDto<?> cacheActionDto,
        Consumer<NotificationHandler> verifier
    ) throws JsonProcessingException {
        // act
        service.synchronizeCacheWithNotification(objectMapper.writeValueAsString(cacheActionDto));

        // assert
        verifier.accept(handler);
    }

    @ParameterizedTest
    @MethodSource("badNotifications")
    void synchronizeCacheWithBadNotification(CacheActionDto<?> cacheActionDto) throws JsonProcessingException {
        // act
        service.synchronizeCacheWithNotification(objectMapper.writeValueAsString(cacheActionDto));

        // assert
        Mockito.verifyNoInteractions(handler);
    }

    static Stream<Arguments> okNotifications() {
        return Stream.of(
            Arguments.of(
                buildCacheAction(CacheAction.INVALIDATE, null),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).invalidate(buildCacheAction(CacheAction.INVALIDATE, null))
            ),
            Arguments.of(
                buildCacheAction(CacheAction.ADD_EMBEDDINGS, new AddEmbeddings(List.of(SERVER_UUID))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).addEmbeddings(Mockito.any())
            ),
            Arguments.of(
                buildCacheAction(CacheAction.REMOVE_EMBEDDINGS, new RemoveEmbeddings(Map.of(SUBJECT_NAME_1, List.of(SERVER_UUID)))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).removeEmbeddings(Mockito.any())
            ),
            Arguments.of(
                buildCacheAction(CacheAction.REMOVE_SUBJECTS, new RemoveSubjects(List.of(SUBJECT_NAME_1))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).removeSubjects(Mockito.any())
            ),
            Arguments.of(
                buildCacheAction(CacheAction.RENAME_SUBJECTS, new RenameSubjects(Map.of(SUBJECT_NAME_1, SUBJECT_NAME_2))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).renameSubjects(Mockito.any())
            ),
            Arguments.of(
                buildCacheAction(CacheAction.UPDATE, new AddEmbeddings(List.of(SERVER_UUID))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).handleUpdate(Mockito.any())
            ),
            Arguments.of(
                buildCacheAction(CacheAction.DELETE, new AddEmbeddings(List.of(SERVER_UUID))),
                (Consumer<NotificationHandler>) h -> Mockito.verify(h, Mockito.only()).handleDelete(Mockito.any())
            )
        );
    }

    static Stream<Arguments> badNotifications() {
        return Stream.of(
            Arguments.of(new CacheActionDto<>(null, API_KEY, SERVER_UUID, null)),
            Arguments.of(new CacheActionDto<>(CacheAction.INVALIDATE, " ", SERVER_UUID, null)),
            Arguments.of(new CacheActionDto<>(CacheAction.INVALIDATE, API_KEY, null, null))
        );
    }

    static <T> CacheActionDto<T> buildCacheAction(CacheAction action, T payload) {
        return new CacheActionDto<>(
            action,
            API_KEY,
            SERVER_UUID,
            payload
        );
    }
}
