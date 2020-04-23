package com.exadel.frs.validation;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.exception.FileExtensionException;
import com.exadel.frs.system.global.ImageProperties;
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

        validator.validate(singletonList(file));

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateFileUppercaseExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("IMAGE.JPG");
        when(imageProperties.getTypes()).thenReturn(List.of("jpg"));

        validator.validate(singletonList(file));

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateEmptyList() {
        validator.validate(emptyList());
    }

    @Test
    public void exceptionIfFileWithWrongExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.tmp");

        assertThrows(FileExtensionException.class, () ->
                validator.validate(singletonList(file))
        );
    }
}