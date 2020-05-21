package com.exadel.frs.core.trainservice.config;

import com.exadel.frs.core.trainservice.filter.SecurityValidationFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@ComponentScan(basePackages = "com.exadel.frs", excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityValidationFilter.class}
))
@MockBeans({@MockBean(MongoTestConfig.class)})
public @interface WebMvcTestContext {

}
