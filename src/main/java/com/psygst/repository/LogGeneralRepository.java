package com.psygst.repository;

import com.psygst.model.LogGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogGeneralRepository extends JpaRepository<LogGeneral, Long> {
}
