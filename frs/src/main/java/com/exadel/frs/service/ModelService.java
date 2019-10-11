package com.exadel.frs.service;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.exception.AppNotFoundException;
import com.exadel.frs.exception.ModelNotFoundException;
import com.exadel.frs.exception.NullAccessTypeException;
import com.exadel.frs.helpers.AccessUpdateType;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.MlModelMapper;
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
    private final SecurityUtils securityUtils;
    private final MlModelMapper modelMapper;

    public ModelDto getModel(Long id, Long clientId) {
        return modelMapper.toDto(modelRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new ModelNotFoundException(id)));
    }

    public List<ModelDto> getModels(Long clientId) {
        return modelRepository.findAllByOwnerId(clientId)
                .stream()
                .map(modelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createModel(ModelDto inputModelDto, Long clientId) {
        inputModelDto.setGuid(UUID.randomUUID().toString());
        inputModelDto.setOwnerId(clientId);
        Model inputModel = modelMapper.toEntity(inputModelDto);
        List<AppModel> appModelList = inputModel.getAppModelList();
        inputModel.setAppModelList(null);
        Model repoModel = modelRepository.save(inputModel);
        if (appModelList != null) {
            appModelList.forEach(appModel -> {
                if (!securityUtils.isClientAppOwner(appModel.getApp().getId(), clientId)) {
                    throw new AppNotFoundException(appModel.getApp().getId());
                }
                appModel.getId().setModelId(repoModel.getId());
                appModel.getModel().setId(repoModel.getId());
            });
            repoModel.setAppModelList(appModelList);
            modelRepository.save(repoModel);
        }
    }

    public void updateModel(Long id, ModelDto inputModelDto, Long clientId) {
        Model inputModel = modelMapper.toEntity(inputModelDto, id);
        Model repoModel = modelRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new ModelNotFoundException(id));
        if (inputModel.getName() != null) {
            repoModel.setName(inputModel.getName());
        }
        if (inputModel.getAppModelList() != null) {
            updatePrivileges(inputModel, repoModel, AccessUpdateType.CLEAN_ADD, clientId);
        }
        modelRepository.save(repoModel);
    }

    public void updatePrivileges(Long id, ModelDto inputModelDto, AccessUpdateType accessUpdateType, Long clientId) {
        Model inputModel = modelMapper.toEntity(inputModelDto, id);
        Model repoModel = modelRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new ModelNotFoundException(id));
        if (inputModel.getAppModelList() != null) {
            updatePrivileges(inputModel, repoModel, accessUpdateType, clientId);
        }
        modelRepository.save(repoModel);
    }

    private void updatePrivileges(Model inputModel, Model repoModel, AccessUpdateType accessUpdateType, Long clientId) {
        if (accessUpdateType == AccessUpdateType.CLEAN_ADD) {
            repoModel.getAppModelList().clear();
        }
        inputModel.getAppModelList().forEach(appModel -> {
            if (!securityUtils.isClientAppOwner(appModel.getApp().getId(), clientId)) {
                throw new AppNotFoundException(appModel.getApp().getId());
            }
            if (accessUpdateType == AccessUpdateType.CLEAN_ADD || accessUpdateType == AccessUpdateType.ADD) {
                if (appModel.getAccessType() == null) {
                    throw new NullAccessTypeException();
                }
                repoModel.getAppModelList().add(appModel);
            }
            if (accessUpdateType == AccessUpdateType.REMOVE) {
                repoModel.getAppModelList().removeIf(repoAppModel -> repoAppModel.getApp().getId().equals(appModel.getApp().getId()));
            }
        });
    }

    public void regenerateGuid(Long id, Long clientId) {
        Model repoModel = modelRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new ModelNotFoundException(id));
        repoModel.setGuid(UUID.randomUUID().toString());
        modelRepository.save(repoModel);
    }

    public void deleteModel(Long id, Long clientId) {
        modelRepository.deleteByIdAndOwnerId(id, clientId);
    }

}
