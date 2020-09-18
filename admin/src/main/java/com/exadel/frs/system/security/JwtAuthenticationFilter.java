/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.system.security;

import com.exadel.frs.entity.User;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private final CustomUserDetailsService userDetailsService;
    @Autowired
    @Lazy
    private TokenStore jdbcTokenStore;

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {
            extractToken(request)
                    .map(jdbcTokenStore::readAuthentication)
                    .map(it -> ((User) it.getPrincipal()).getUsername())
                    .map(userDetailsService::loadUserByUsername)
                    .map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
                    .ifPresent(token -> authenticate(token, request));
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTH_HEADER))
                       .filter(v -> v.startsWith(TOKEN_PREFIX))
                       .map(v -> v.substring(TOKEN_PREFIX.length()));
    }

    private void authenticate(UsernamePasswordAuthenticationToken token, HttpServletRequest request) {
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}