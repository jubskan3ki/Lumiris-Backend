package com.minoh.lumiris_backend.service;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.dto.out.DppFormSummaryResponse;
import com.minoh.lumiris_backend.entity.DppForm;
import com.minoh.lumiris_backend.entity.User;
import com.minoh.lumiris_backend.exception.ResourceNotFoundException;
import com.minoh.lumiris_backend.mapper.DppFormMapper;
import com.minoh.lumiris_backend.repository.DppFormRepository;
import com.minoh.lumiris_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DppFormService {

    private final DppFormRepository dppFormRepository;
    private final UserRepository userRepository;
    private final DppFormMapper dppFormMapper;
    private final QuotaService quotaService;

    @Transactional
    public DppFormResponse create(DppFormRequest request, String userEmail) {
        User user = userRepository.getByEmail(userEmail);

        // Gate: requires an active passport-granting subscription within quota.
        quotaService.assertCanCreate(user);

        DppForm form = dppFormMapper.toEntity(request, user);
        DppForm saved = dppFormRepository.save(form);
        return dppFormMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DppFormSummaryResponse> findAllByUser(String userEmail, Pageable pageable) {
        User user = userRepository.getByEmail(userEmail);
        return dppFormRepository.findByUserId(user.getId(), pageable)
                .map(dppFormMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public DppFormResponse findById(UUID id, String userEmail) {
        User user = userRepository.getByEmail(userEmail);
        DppForm form = dppFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DPP not found"));
        if (!form.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("DPP not found");
        }
        initializeChildCollections(form);
        return dppFormMapper.toResponse(form);
    }

    // The three @OneToMany List (bag) collections can't be fetch-joined in a single query
    // (Hibernate MultipleBagFetchException), so we initialise each explicitly. They are small,
    // bounded child collections of one DPP.
    private void initializeChildCollections(DppForm form) {
        Hibernate.initialize(form.getMaterials());
        Hibernate.initialize(form.getCareInstructions());
        Hibernate.initialize(form.getCertifications());
    }
}
