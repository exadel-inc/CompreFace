package com.exadel.frs.controller;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping("/{id}")
    public ModelDto getModel(@PathVariable Long id) {
        return modelService.getModel(id);
    }

    @GetMapping("/")
    public List<ModelDto> getModels() {
        return modelService.getModels();
    }

    @PostMapping("/")
    @Transactional
    public void createModel(@Valid @RequestBody ModelDto inputModelDto) {
        modelService.createModel(inputModelDto);
    }

    @PutMapping("/{id}")
    public void updateModel(@PathVariable Long id, @Valid @RequestBody ModelDto inputModelDto) {
        modelService.updateModel(id, inputModelDto);
    }

    @PutMapping("/{id}/guid")
    public void regenerateGuid(@PathVariable Long id) {
        modelService.regenerateGuid(id);
    }

    @DeleteMapping("/{id}")
    public void deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
    }

}
