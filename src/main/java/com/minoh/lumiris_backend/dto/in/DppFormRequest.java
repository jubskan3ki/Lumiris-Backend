package com.minoh.lumiris_backend.dto.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

// All fields are optional (a DPP can be saved as an empty draft); constraints only bound format/range/length.
public record DppFormRequest(
        @Size(max = 255) String productName,
        @Size(max = 4000) String productDescription,
        @Size(max = 255) String productCategory,
        @Size(max = 255) String originCountry,
        @Size(max = 50) List<@Size(max = 100) String> availableSizes,
        @Size(max = 50) List<@Size(max = 100) String> colors,
        @Size(max = 2048) String mainPhotoUrl,

        @Valid @Size(max = 100) List<MaterialRequest> materials,
        @Size(max = 100) List<@Size(max = 100) String> careInstructions,
        @Valid @Size(max = 100) List<CertificationRequest> certifications,

        @Size(max = 255) String manufacturedAt,
        @Size(max = 255) String batchNumber,
        @Pattern(regexp = "^(\\d{8,14})?$", message = "GTIN must be 8 to 14 digits") String gtin,
        @Size(max = 255) String sku,
        Boolean reachCompliant,

        @Min(0) @Max(100) Integer recycledPct,
        @Size(max = 2000) String warrantyDescription,
        Boolean isRepairable,
        @Size(max = 2000) String endOfLifeInstructions
) {}
