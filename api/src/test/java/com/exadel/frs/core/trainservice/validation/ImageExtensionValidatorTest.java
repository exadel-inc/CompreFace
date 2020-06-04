package com.exadel.frs.core.trainservice.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.exception.FileExtensionException;
import com.exadel.frs.core.trainservice.system.global.ImageProperties;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

class ImageExtensionValidatorTest {

    @Mock
    private ImageProperties imageProperties;

    @InjectMocks
    private ImageExtensionValidator validator;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    public void validateFileWithExpectedExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.jpg");
        when(imageProperties.getTypes()).thenReturn(List.of("jpg"));

        validator.validate(file);

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateFileUppercaseExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("IMAGE.JPG");
        when(imageProperties.getTypes()).thenReturn(List.of("jpg"));

        validator.validate(file);

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateNull() {
        validator.validate(null);
    }

    @Test
    public void exceptionIfFileWithWrongExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.tmp");

        assertThrows(FileExtensionException.class, () ->
                validator.validate(file)
        );
    }
}