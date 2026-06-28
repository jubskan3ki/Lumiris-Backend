package com.minoh.lumiris_backend.controller;

import com.minoh.lumiris_backend.config.security.CurrentUserEmail;
import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.dto.out.DppFormSummaryResponse;
import com.minoh.lumiris_backend.service.DppFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dpp-forms")
@RequiredArgsConstructor
public class DppFormController {

    private final DppFormService dppFormService;

    @PostMapping
    ResponseEntity<DppFormResponse> create(
            @Valid @RequestBody(required = false) DppFormRequest request,
            @CurrentUserEmail String email
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dppFormService.create(request, email));
    }

    @GetMapping
    ResponseEntity<Page<DppFormSummaryResponse>> findAll(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @CurrentUserEmail String email
    ) {
        return ResponseEntity.ok(dppFormService.findAllByUser(email, pageable));
    }

    @GetMapping("/{id}")
    ResponseEntity<DppFormResponse> findById(
            @PathVariable UUID id,
            @CurrentUserEmail String email
    ) {
        return ResponseEntity.ok(dppFormService.findById(id, email));
    }
}
