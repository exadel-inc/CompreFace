package com.exadel.frs.core.trainservice.dto;

import static com.exadel.frs.core.trainservice.system.global.Constants.RECOGNIZE_EMBEDDINGS_DESC;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRecognitionRequestDto {

    @NotEmpty
    @ApiParam(value = RECOGNIZE_EMBEDDINGS_DESC, required = true)
    private List<double[]> embeddings;
}
