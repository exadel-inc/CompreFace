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

public class FaceClassifierStorageLocal implements FaceClassifierStorage{

  private ApplicationContext context;

  private Map<Pair<String, String>, FaceClassifierProxy> classifierTrainerMap;

  private Map<Pair<String, String>, AtomicBoolean> locks;

  public static final String FACE_CLASSIFIER_PROXY_NAME = "faceClassifierProxy";

  @Autowired
  public FaceClassifierStorageLocal(ApplicationContext context) {
    this.context = context;
  }

  @PostConstruct
  public void postConstruct() {
    classifierTrainerMap = new ConcurrentHashMap<>();
    locks = new ConcurrentHashMap<>();
  }

  public FaceClassifierProxy getFaceClassifier(String appKey, String modelId) {
    return classifierTrainerMap.computeIfAbsent(Pair.of(appKey, modelId),stringStringPair ->
        (FaceClassifierProxy) context.getBean(FACE_CLASSIFIER_PROXY_NAME));
  }

  public void lock(String appKey, String modelId){
    AtomicBoolean lock = locks
        .computeIfAbsent(Pair.of(appKey, modelId),stringStringPair ->  new AtomicBoolean(false));
    if (lock.get()){
      throw new ModelAlreadyLockedException("Previous retraining has not been finished yet");
    }
    lock.set(true);
  }

  public void unlock(String appKey, String modelId){
    AtomicBoolean lock = locks
        .getOrDefault(Pair.of(appKey, modelId), new AtomicBoolean(false));
    if (lock.get()){
      for (Thread thread : Thread.getAllStackTraces().keySet()){
        if (thread.getName().equals(appKey + modelId)){
          thread.interrupt();
        }
      }
    }
    lock.set(false);
  }

  public boolean isLocked(String appKey, String modelId){
    return locks
        .getOrDefault(Pair.of(appKey, modelId), new AtomicBoolean(false)).get();
  }

}
