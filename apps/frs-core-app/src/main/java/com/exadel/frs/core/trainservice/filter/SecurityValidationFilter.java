package com.exadel.frs.core.trainservice.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Filter created to validate if this application has access to requested model
 */

@Component
@Order(1)
public class SecurityValidationFilter implements Filter {

  private static final String APP_KEY = "apikey";
  private static final String MODEL_ID = "modelid";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

    Map<String, List<String>> headersMap = Collections
        .list(httpRequest.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(
            Function.identity(),
            h -> Collections.list(httpRequest.getHeaders(h))
        ));

    List<String> appKey = headersMap.getOrDefault(APP_KEY, Collections.emptyList());
    List<String> modelId = headersMap.getOrDefault(MODEL_ID, Collections.emptyList());
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
