package com.exadel.frs.aspect;

import com.exadel.frs.annotation.Statistics;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.StatisticsType;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.feign.StatisticsDatabaseClient;
import com.exadel.frs.system.feign.StatisticsGeneralEntity;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@EnableAspectJAutoProxy
@Component
@RequiredArgsConstructor
public class StatisticAspect {

    @Value("${app.feign.appery-io.database.id}")
    private String statisticsDatabaseId;
    private final UserService userService;
    private final StatisticsDatabaseClient statisticsDatabaseClient;

    @SneakyThrows
    @AfterReturning(pointcut = "@annotation(com.exadel.frs.annotation.Statistics)", returning = "result")
    public void afterMethodInvocation(JoinPoint joinPoint, Object result) {
        if (StringUtils.isEmpty(statisticsDatabaseId)) {
            return;
        }

        User user;

        if (User.class.equals(result.getClass())) {
            user = (User) result;
        } else {
            user = userService.getUser(SecurityUtils.getPrincipalId());
        }

        if (!user.isAllowStatistics()) {
            return;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        Statistics statistics = joinPoint.getTarget().getClass().getMethod(methodName, parameterTypes).getAnnotation(Statistics.class);
        StatisticsType statisticsType = statistics.type();

        statisticsDatabaseClient.create(statisticsDatabaseId, new StatisticsGeneralEntity(user.getGuid(), statisticsType));
    }
}
