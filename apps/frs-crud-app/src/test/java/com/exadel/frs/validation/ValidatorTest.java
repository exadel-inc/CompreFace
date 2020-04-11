package com.exadel.frs.validation;

import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validator;

@SpringBootTest
public class ValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    public void testUserRoleUpdateDtoEmpty() {
        val actual = validator.validate(UserRoleUpdateDto.builder().build());

        Assertions.assertFalse(actual.isEmpty());
    }

    @Test
    public void testUserRoleUpdateDto() {
        val actual = validator.validate(UserRoleUpdateDto.builder()
                .role("test")
                .userId("test")
                .build());

        Assertions.assertTrue(actual.isEmpty());
    }


}
