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

package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.GlobalRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndEnabledTrue(String email);

    Optional<User> findByGuid(String guid);

    boolean existsByEmail(String email);

    boolean existsByGuid(String guid);

    @Query("from User where email like :q or firstName like :q or lastName like :q")
    List<User> autocomplete(String q);

    User findByGlobalRole(GlobalRole role);

    @Query("select count(u) > 0 from User u where u.globalRole = 'O'")
    boolean isOwnerPresent();

    int deleteByEnabledFalseAndRegTimeBefore(LocalDateTime time);

    Optional<User> findByRegistrationToken(String token);

    void deleteByGuid(String userGuid);
}