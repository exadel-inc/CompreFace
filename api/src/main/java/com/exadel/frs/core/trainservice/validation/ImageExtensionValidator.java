package com.exadel.frs.core.trainservice.validation;

import static com.google.common.io.Files.getFileExtension;
import com.exadel.frs.core.trainservice.exception.FileExtensionException;
import com.exadel.frs.core.trainservice.system.global.ImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageExtensionValidator {

    private final ImageProperties imageProperties;

    public void validate(final MultipartFile file) {
        if (file == null) {
            return;
        }

        val formats = imageProperties.getTypes();
        val isWrongFormat = !formats.contains(
                getFileExtension(file.getOriginalFilename().toLowerCase())
        );

        if (isWrongFormat) {
            throw new FileExtensionException(file.getOriginalFilename());
        }
    }
}