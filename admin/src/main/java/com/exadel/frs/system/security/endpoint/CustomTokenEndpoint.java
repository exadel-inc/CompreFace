package com.exadel.frs.system.security.endpoint;

import static com.exadel.frs.system.global.Constants.ACCESS_TOKEN_COOKIE_NAME;
import com.exadel.frs.entity.User;
import java.security.Principal;
import java.util.Map;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/oauth/token")
public class CustomTokenEndpoint extends TokenEndpoint {

    @PostMapping
    public ResponseEntity<OAuth2AccessToken> postAccessToken(
            Principal principal,
            @RequestParam Map<String, String> parameters
    ) throws HttpRequestMethodNotSupportedException {

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            if (((UsernamePasswordAuthenticationToken) principal).getPrincipal() instanceof User) {
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        }

        val defaultResponse = super.postAccessToken(principal, parameters);
        val defaultToken = defaultResponse.getBody();

        val cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, defaultToken.getValue())
                                   .httpOnly(true)
                                   .maxAge(defaultToken.getExpiresIn())
                                   .path("/admin")
                                   .build();
        val headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
