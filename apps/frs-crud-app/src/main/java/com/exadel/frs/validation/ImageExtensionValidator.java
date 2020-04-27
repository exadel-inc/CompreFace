package com.exadel.frs.validation;

import static com.google.common.io.Files.getFileExtension;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.exception.FileExtensionException;
import com.exadel.frs.system.global.ImageProperties;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageExtensionValidator {

    private final ImageProperties imageProperties;

    public void validate(final Collection<MultipartFile> files) {
        val formats = imageProperties.getTypes();
        val wrongFileNames = files.stream()
                                  .filter(file ->
                                          !formats.contains(
                                                  getFileExtension(file.getOriginalFilename().toLowerCase())
                                          )
                                  )
                                  .map(file -> file.getOriginalFilename())
                                  .collect(toList());

        if (isNotEmpty(wrongFileNames)) {
            throw new FileExtensionException(wrongFileNames);
        }
    }
}