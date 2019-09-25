package com.exadel.frs.service;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final AppMapper appMapper = AppMapper.INSTANCE;

    public AppDto getApp(Long id) {
        return appMapper.toDto(appRepository.findById(id).orElseThrow());
    }

    public List<AppDto> getApps() {
        return appRepository.findAll()
                .stream()
                .map(appMapper::toDto)
                .collect(Collectors.toList());
    }

    public void createApp(AppDto inputAppDto) {
        inputAppDto.setModelAccess(null);
        inputAppDto.setGuid(UUID.randomUUID().toString());
        appRepository.save(appMapper.toEntity(inputAppDto));
    }

    public void updateApp(Long id, AppDto inputAppDto) {
        inputAppDto.setModelAccess(null);
        App inputApp = appMapper.toEntity(inputAppDto, id);
        App repoApp = appRepository.findById(id).orElseThrow();
        if (inputApp.getName() != null) {
            repoApp.setName(inputApp.getName());
        }
        appRepository.save(repoApp);
    }

    public void regenerateGuid(Long id) {
        App repoApp = appRepository.findById(id).orElseThrow();
        repoApp.setGuid(UUID.randomUUID().toString());
        appRepository.save(repoApp);
    }

    public void deleteApp(Long id) {
        appRepository.deleteById(id);
    }

}
