package com.exadel.frs.service;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.mapper.ModelMapper;
import com.exadel.frs.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper = ModelMapper.INSTANCE;

    public ModelDto getModel(Long id) {
        return modelMapper.toDto(modelRepository.findById(id).orElseThrow());
    }

    public List<ModelDto> getModels() {
        return modelRepository.findAll()
                .stream()
                .map(modelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createModel(ModelDto inputModelDto) {
        inputModelDto.setGuid(UUID.randomUUID().toString());
        Model inputModel = modelMapper.toEntity(inputModelDto);
        List<AppModel> appModelList = inputModel.getAppModelList();
        inputModel.setAppModelList(null);
        Model repoModel = modelRepository.save(inputModel);
        if (appModelList != null) {
            appModelList.forEach(item -> {
                item.getId().setModelId(repoModel.getId());
                item.getModel().setId(repoModel.getId());
            });
            repoModel.setAppModelList(appModelList);
            modelRepository.save(repoModel);
        }
    }

    public void updateModel(Long id, ModelDto inputModelDto) {
        Model inputModel = modelMapper.toEntity(inputModelDto, id);
        Model repoModel = modelRepository.findById(id).orElseThrow();
        if (inputModel.getName() != null) {
            repoModel.setName(inputModel.getName());
        }
        if (inputModel.getAppModelList() != null) {
            repoModel.getAppModelList().clear();
            inputModel.getAppModelList().forEach(item -> repoModel.getAppModelList().add(item));
        }
        modelRepository.save(repoModel);
    }

    public void regenerateGuid(Long id) {
        Model repoModel = modelRepository.findById(id).orElseThrow();
        repoModel.setGuid(UUID.randomUUID().toString());
        modelRepository.save(repoModel);
    }

    public void deleteModel(Long id) {
        modelRepository.deleteById(id);
    }

}
