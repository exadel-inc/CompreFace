package com.exadel.frs.helpers;

import com.exadel.frs.entity.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public final class SecurityUtils {

  public User getPrincipal() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

}
