package com.exadel.frs.system.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Log4j2
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            String principal = getAuthData(authentication.getPrincipal());
            String credentials = getAuthData(authentication.getCredentials());
            return new UsernamePasswordAuthenticationToken(principal, credentials, authentication.getAuthorities());
        } catch (UnsupportedEncodingException e) {
            log.error("Error during authentication", e);
            return new UsernamePasswordAuthenticationToken(null, null, authentication.getAuthorities());
        }
    }

    private String getAuthData(Object value) throws UnsupportedEncodingException {
        if (value == null) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        byte[] base64AuthData = value.toString().getBytes(StandardCharsets.UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64AuthData);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode auth data token", e);
            return value.toString();
        }
        return new String(decoded, StandardCharsets.UTF_8);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
