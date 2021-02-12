package com.exadel.frs.aspect;

import com.exadel.frs.annotation.CollectStatistics;
import com.exadel.frs.commonservice.entity.InstallInfo;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.commonservice.enums.StatisticsType;
import com.exadel.frs.exception.ApperyServiceException;
import com.exadel.frs.repository.InstallInfoRepository;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.system.feign.ApperyStatisticsClient;
import com.exadel.frs.system.feign.StatisticsGeneralEntity;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsCollectionAspect {

    @Value("${app.feign.appery-io.api-key}")
    private String statisticsApiKey;
    private final ApperyStatisticsClient apperyStatisticsClient;
    private final InstallInfoRepository installInfoRepository;
    private final UserRepository userRepository;

    private InstallInfo installInfo;

    private String getInstallGuid() {
        if (installInfo == null) {
            installInfo = installInfoRepository.findTopByOrderByInstallGuid();
        }

        return installInfo.getInstallGuid();
    }

    @SneakyThrows
    @AfterReturning(pointcut = "@annotation(com.exadel.frs.annotation.CollectStatistics)", returning = "result")
    public void afterMethodInvocation(JoinPoint joinPoint, Object result) {
        if (StringUtils.isEmpty(statisticsApiKey)) {
            return;
        }

        User user = userRepository.findByGlobalRole(GlobalRole.OWNER);

        if (user == null || !user.isAllowStatistics()) {
            return;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        CollectStatistics collectStatistics = joinPoint.getTarget().getClass().getMethod(methodName, parameterTypes).getAnnotation(
                CollectStatistics.class);
        StatisticsType statisticsType = collectStatistics.type();

        try {
            apperyStatisticsClient.create(
                    statisticsApiKey,
                    new StatisticsGeneralEntity(getInstallGuid(), statisticsType)
            );
        } catch (FeignException exception) {
            throw new ApperyServiceException();
        }
    }
}