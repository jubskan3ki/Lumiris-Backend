package com.minoh.lumiris_backend.dto.out;

import java.util.UUID;

public record DppVerificationResponse(
        UUID id,
        boolean verified,
        String storedHash,
        String recomputedHash
) {}