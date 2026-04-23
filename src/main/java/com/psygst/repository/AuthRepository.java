package com.psygst.repository;

import com.psygst.model.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String> {
    Optional<Auth> findByUsernameAndBaja(String username, Boolean baja);
    Optional<Auth> findByUsername(String username);
}
