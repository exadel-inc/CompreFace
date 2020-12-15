package com.exadel.frs.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import com.exadel.frs.enums.StatisticsType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Set annotation if you want to save action in statistic
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface Statistics {

    StatisticsType type();
}
