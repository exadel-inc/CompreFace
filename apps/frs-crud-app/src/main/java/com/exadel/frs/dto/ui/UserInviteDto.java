package com.exadel.frs.dto.ui;

import com.exadel.frs.enums.AppRole;
import lombok.Data;

@Data
public class UserInviteDto {

    private AppRole role;
    private String userEmail;

}
