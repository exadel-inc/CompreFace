package com.exadel.frs.service;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.exception.AppNotFoundException;
import com.exadel.frs.exception.EmptyAppNameException;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final AppMapper appMapper;

    public AppDto getApp(Long id, Long clientId) {
        return appMapper.toDto(appRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new AppNotFoundException(id)));
    }

    public List<AppDto> getApps(Long clientId) {
        return appRepository.findAllByOwnerId(clientId)
                .stream()
                .map(appMapper::toDto)
                .collect(Collectors.toList());
    }

    public void createApp(AppDto inputAppDto, Long clientId) {
        if (StringUtils.isEmpty(inputAppDto.getName())) {
            throw new EmptyAppNameException();
        }
        inputAppDto.setGuid(UUID.randomUUID().toString());
        inputAppDto.setOwnerId(clientId);
        appRepository.save(appMapper.toEntity(inputAppDto));
    }

    public void updateApp(Long id, AppDto inputAppDto, Long clientId) {
        App inputApp = appMapper.toEntity(inputAppDto, id);
        App repoApp = appRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new AppNotFoundException(id));
        if (!StringUtils.isEmpty(inputApp.getName())) {
            repoApp.setName(inputApp.getName());
        }
        appRepository.save(repoApp);
    }

    public void regenerateGuid(Long id, Long clientId) {
        App repoApp = appRepository.findByIdAndOwnerId(id, clientId)
                .orElseThrow(() -> new AppNotFoundException(id));
        repoApp.setGuid(UUID.randomUUID().toString());
        appRepository.save(repoApp);
    }

    public void deleteApp(Long id, Long clientId) {
        appRepository.deleteByIdAndOwnerId(id, clientId);
    }

}
