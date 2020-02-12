package com.exadel.frs.validation;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.exadel.frs.exception.FileExtensionException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class ImageExtensionValidatorTest {

    @Test
    public void validateFileWithExpectedExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.jpg");

        new ImageExtensionValidator().validate(singletonList(file));
    }

    @Test
    public void validateEmptyList() {
        new ImageExtensionValidator().validate(emptyList());
    }

    @Test
    public void exceptionIfFileWithWrongExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.tmp");

        assertThrows(FileExtensionException.class, () ->
                new ImageExtensionValidator().validate(singletonList(file))
        );
    }
}