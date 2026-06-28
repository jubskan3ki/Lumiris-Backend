package com.minoh.lumiris_backend.dto.in;

import jakarta.validation.constraints.Size;

public record CertificationRequest(
        @Size(max = 255) String name,
        @Size(max = 255) String customName,
        @Size(max = 255) String licenseNumber
) {}
