package com.exadel.frs.core.trainservice.config;

import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorageLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

  @Bean
  public FaceClassifierStorage storage(@Autowired ApplicationContext context) {
    return new FaceClassifierStorageLocal(context);
  }
}
