package com.exadel.frs.core.trainservice.system;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenParts {
    private String appApiKey;
    private String modelApiKey;
}
