package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.component.FaceClassifierProxy;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;

public interface FaceClassifierStorage {

  FaceClassifierProxy getFaceClassifier(String appKey, String modelId);

  /**
   * Throws {@link ModelAlreadyLockedException} if model already locked
   * */
  void lock(String appKey, String modelId) throws ModelAlreadyLockedException;

  void unlock(String appKey, String modelId);

  boolean isLocked(String appKey, String modelId);
}
