package com.exadel.frs.core.trainservice.filter;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter created to validate if this application has access to requested model
 */

@Component
@Order(1)
public class SecurityValidationFilter implements Filter {

    private static final String APP_KEY = "apikey";
    private static final String MODEL_ID = "modelid";

    @Override
    public void init(final FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain
    ) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;

        Map<String, List<String>> headersMap = Collections
                .list(httpRequest.getHeaderNames())
                .stream()
                .collect(toMap(
                        Function.identity(),
                        h -> Collections.list(httpRequest.getHeaders(h))
                ));

        var appKey = headersMap.getOrDefault(APP_KEY, emptyList());
        var modelId = headersMap.getOrDefault(MODEL_ID, emptyList());
        if (appKey.size() > 1 || modelId.size() > 1) {
            throw new RuntimeException("Multiple appkey or modelId exist");
        }

        if (appKey.size() == 1 || modelId.size() == 1) {
            //@todo security check
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}