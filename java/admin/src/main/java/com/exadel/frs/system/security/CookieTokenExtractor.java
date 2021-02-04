package com.exadel.frs.system.security;

import static com.exadel.frs.system.global.Constants.ACCESS_TOKEN_COOKIE_NAME;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.util.WebUtils;

public class CookieTokenExtractor implements TokenExtractor {

    @Override
    public Authentication extract(HttpServletRequest request) {
        return extractToken(request).map(s -> new PreAuthenticatedAuthenticationToken(s, ""))
                                    .orElse(null);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME)).map(Cookie::getValue);
    }
}
