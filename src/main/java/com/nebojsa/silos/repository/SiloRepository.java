package com.nebojsa.silos.repository;

import com.nebojsa.silos.entity.Silo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiloRepository extends JpaRepository<Silo, Long> {
}
