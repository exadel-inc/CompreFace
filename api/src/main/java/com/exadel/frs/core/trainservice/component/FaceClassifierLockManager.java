/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FaceClassifierLockManager {

    private Map<String, AtomicBoolean> locks;

    @Getter
    private volatile CountDownLatch countDownLatch;

    @PostConstruct
    public void postConstruct() {
        locks = new ConcurrentHashMap<>();
        countDownLatch = new CountDownLatch(0);
    }

    public synchronized void lock(String modelKey) {
        var lock = locks
                .computeIfAbsent(modelKey, stringStringPair -> new AtomicBoolean(false));
        if (lock.get()) {
            throw new ModelAlreadyLockedException("Previous retraining has not been finished yet");
        }

        lock.set(true);
        countDownLatch = new CountDownLatch((int) (countDownLatch.getCount() + 1));
        log.debug("Model {} locked, models locked : {}", modelKey, countDownLatch.getCount());
    }

    public synchronized void unlock(final String modelKey) {
        val lock = locks
                .getOrDefault(modelKey, new AtomicBoolean(false));
        if (lock.get()) {
            for (val thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().equals(modelKey)) {
                    thread.interrupt();
                }
            }
        }

        lock.set(false);
        countDownLatch.countDown();
        log.debug("Model {} unlocked, models locked : {}", modelKey, countDownLatch.getCount());
    }

    public boolean isLocked(final String modelKey) {
        return locks
                .getOrDefault(modelKey, new AtomicBoolean(false)).get();
    }
}