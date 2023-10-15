package com.exadel.frs.system.security.endpoint;

import static com.exadel.frs.system.global.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.exadel.frs.system.global.Constants.ADMIN;
import static com.exadel.frs.system.global.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

@FrameworkEndpoint
@RequiredArgsConstructor
public class RevokeTokenEndpoint {

    private final DefaultTokenServices tokenServices;

    @ResponseBody
    @DeleteMapping(ADMIN + "/oauth/token")
    public ResponseEntity<Void> revokeToken(HttpServletRequest request) {
        if (isAccessTokenBlank(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        val accessToken = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME).getValue();
        tokenServices.revokeToken(accessToken);

        val accessTokenCookie = buildEmptyCookie(ACCESS_TOKEN_COOKIE_NAME, "/admin");
        val refreshTokenCookie = buildEmptyCookie(REFRESH_TOKEN_COOKIE_NAME, "/admin/oauth/token");

        val headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie);
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie);

        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }

    private String buildEmptyCookie(String name, String path) {
        return ResponseCookie.from(name, EMPTY)
                             .httpOnly(true)
                             .maxAge(0)
                             .path(path)
                             .build()
                             .toString();
    }

    private boolean isAccessTokenBlank(HttpServletRequest request) {
        val cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        val cookieValue = Optional.ofNullable(cookie)
                                  .map(Cookie::getValue)
                                  .orElse(EMPTY);

        return StringUtils.isBlank(cookieValue);
    }
}
