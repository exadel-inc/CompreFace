package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.dto.DbActionDto;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbActionService {

    private final FaceCacheProvider faceCacheProvider;
    private final FacesRepository facesRepository;

    public void synchronizeCache(DbActionDto action) {
        if (!action.getServerUUID().equals(SERVER_UUID)) {
            switch (action.getAction()) {
                case DELETE:
                    action.getFaceIds()
                          .forEach(face -> faceCacheProvider.getOrLoad(action.getApiKey())
                                                            .removeFace(face, action.getFaceName())
                          );
                    break;
                case INSERT:
                    faceCacheProvider.getOrLoad(action.getApiKey())
                                     .addFace(facesRepository.findById(action.getFaceIds().get(0)).get());
                    break;
                case DELETE_ALL:
                    faceCacheProvider.invalidate(action.getApiKey());
                    break;
            }
        }
    }
}
