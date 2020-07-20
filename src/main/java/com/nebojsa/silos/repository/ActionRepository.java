package com.nebojsa.silos.repository;

import com.nebojsa.silos.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends RevisionRepository<Action, Long, Integer>, JpaRepository<Action, Long> {

    List<Action> findAllByFrom_IdAndUpdatedInMillisIsLessThanEqualOrderByUpdatedInMillisAsc(Long siloFromId, Long updatedInMillis);

    List<Action> findAllByTo_IdAndUpdatedInMillisIsLessThanEqualOrderByUpdatedInMillisAsc(Long siloFromId, Long updatedInMillis);

}
