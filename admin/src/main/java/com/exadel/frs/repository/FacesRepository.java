package com.exadel.frs.repository;


import com.exadel.frs.entity.Face;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface FacesRepository extends JpaRepository<Face, Long> {

    List<Face> findByApiKey(String modelApiKey);

}