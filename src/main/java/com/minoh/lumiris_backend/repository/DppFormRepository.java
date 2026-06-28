package com.minoh.lumiris_backend.repository;

import com.minoh.lumiris_backend.entity.DppForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DppFormRepository extends JpaRepository<DppForm, UUID> {

    Page<DppForm> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);
}
