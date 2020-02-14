package com.exadel.frs.validation;

import static com.google.common.io.Files.getFileExtension;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.exception.FileExtensionException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Collection;
import lombok.val;
import org.springframework.web.multipart.MultipartFile;

public class ImageExtensionValidator {

    private static final Config config = ConfigFactory.load("images.conf");

    public void validate(final Collection<MultipartFile> files) {
        val formats = config.getStringList("image.fileTypes");

        val wrongFileNames = files.stream()
                                  .filter(file ->
                                          !formats.contains(
                                                  getFileExtension(file.getOriginalFilename())
                                          )
                                  )
                                  .map(file -> file.getOriginalFilename())
                                  .collect(toList());

        if (isNotEmpty(wrongFileNames)) {
            throw new FileExtensionException(wrongFileNames);
        }

        return;
    }
}