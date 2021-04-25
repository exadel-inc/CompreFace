package com.exadel.frs.commonservice.annotation;

import com.exadel.frs.commonservice.enums.StatisticsType;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Set annotation if you want to save action in statistic
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface CollectStatistics {

    StatisticsType type();
}