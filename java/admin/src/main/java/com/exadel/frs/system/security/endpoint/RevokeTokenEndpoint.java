package com.exadel.frs.system.security.endpoint;

import static com.exadel.frs.system.global.Constants.ACCESS_TOKEN_COOKIE_NAME;
import javax.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

@FrameworkEndpoint
public class RevokeTokenEndpoint {

    @Autowired
    private DefaultTokenServices tokenServices;

    @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
    @ResponseBody
    public ResponseEntity revokeToken(HttpServletRequest request) {
        if (WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        val token = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME).getValue();
        tokenServices.revokeToken(token);

        val cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                                   .httpOnly(true)
                                   .maxAge(0)
                                   .path("/admin")
                                   .build();
        val headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
