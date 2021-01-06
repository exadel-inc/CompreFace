package com.exadel.frs.aspect;

import static com.exadel.frs.enums.StatisticsType.USER_CREATE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.annotation.CollectStatistics;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.StatisticsType;
import com.exadel.frs.exception.ApperyServiceException;
import com.exadel.frs.system.feign.ApperyStatisticsClient;
import com.exadel.frs.system.feign.StatisticsGeneralEntity;
import feign.FeignException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;

class StatisticsCollectionAspectTest {

    private final static String STATISTICS_API_KEY = "statisticsApiKey";

    @Mock
    private ApperyStatisticsClient apperyStatisticsClient;

    private JoinPoint joinPoint;
    private MethodSignature signature;
    private Method method;
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
    private Authentication authentication;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        initMocks(this);
        joinPoint = mock(JoinPoint.class);
        signature = mock(MethodSignature.class);
        method = TestingCollectStatistics.class.getMethod("createUser");
        usernamePasswordAuthenticationToken = mock(UsernamePasswordAuthenticationToken.class);
        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        authentication = SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void afterMethodInvocationWhenUserIsAuthenticated() {
        //given
        User user = User.builder()
                        .allowStatistics(true)
                        .build();
        val statisticsCollectionAspect = createStatisticsCollectionAspect(STATISTICS_API_KEY);

        when(usernamePasswordAuthenticationToken.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new TestingCollectStatistics());

        //when
        statisticsCollectionAspect.afterMethodInvocation(joinPoint, user);

        //then
        verify(apperyStatisticsClient).create(STATISTICS_API_KEY, new StatisticsGeneralEntity(user.getGuid(), StatisticsType.USER_CREATE));
        verifyNoMoreInteractions(apperyStatisticsClient);
    }

    @Test
    void afterMethodInvocationWhenUserIsNotAuthenticated() {
        //given
        User user = User.builder()
                        .allowStatistics(true)
                        .build();
        val statisticsCollectionAspect = createStatisticsCollectionAspect(STATISTICS_API_KEY);

        when(usernamePasswordAuthenticationToken.isAuthenticated()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new TestingCollectStatistics());

        //when
        statisticsCollectionAspect.afterMethodInvocation(joinPoint, user);

        //then
        verify(apperyStatisticsClient).create(STATISTICS_API_KEY, new StatisticsGeneralEntity(user.getGuid(), StatisticsType.USER_CREATE));
        verifyNoMoreInteractions(apperyStatisticsClient);
    }

    @Test
    void afterMethodInvocationWhenStatisticsApiKeyIsEmpty() {
        User user = User.builder()
                        .allowStatistics(true)
                        .build();
        val statisticsCollectionAspect = createStatisticsCollectionAspect(EMPTY);

        //when
        statisticsCollectionAspect.afterMethodInvocation(joinPoint, user);

        //then
        verify(usernamePasswordAuthenticationToken, times(0)).isAuthenticated();
    }

    @Test
    void afterMethodInvocationWhenStatisticsIsNotAllowed() {
        User user = User.builder()
                        .allowStatistics(false)
                        .build();
        val statisticsCollectionAspect = createStatisticsCollectionAspect(STATISTICS_API_KEY);

        //when
        statisticsCollectionAspect.afterMethodInvocation(joinPoint, user);

        //then
        verify(signature, times(0)).getMethod();
    }

    @Test
    void afterMethodInvocationWhenApperyStatisticsClientIsUnavailable() {
        //given
        User user = User.builder()
                        .allowStatistics(true)
                        .build();
        val statisticsCollectionAspect = createStatisticsCollectionAspect(STATISTICS_API_KEY);
        StatisticsGeneralEntity statisticsGeneralEntity = new StatisticsGeneralEntity(user.getGuid(), USER_CREATE);

        when(usernamePasswordAuthenticationToken.isAuthenticated()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new TestingCollectStatistics());

        //when
        doThrow(FeignException.class)
                .when(apperyStatisticsClient).create(STATISTICS_API_KEY, statisticsGeneralEntity);

        //then
        assertThatThrownBy(() -> statisticsCollectionAspect.afterMethodInvocation(joinPoint, user))
                .isInstanceOf(ApperyServiceException.class);
    }

    private static class TestingCollectStatistics {

        @CollectStatistics(type = USER_CREATE)
        public User createUser() {
            return User.builder()
                       .build();
        }
    }

    @SneakyThrows
    private StatisticsCollectionAspect createStatisticsCollectionAspect(String statisticsApiKey) {
        val statisticsCollectionAspect = new StatisticsCollectionAspect(apperyStatisticsClient);
        ReflectionTestUtils.setField(statisticsCollectionAspect, STATISTICS_API_KEY, statisticsApiKey);
        return statisticsCollectionAspect;
    }
}