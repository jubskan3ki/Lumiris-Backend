package com.minoh.lumiris_backend.exception;

import com.minoh.lumiris_backend.dto.out.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return new ErrorResponse(400, "Validation failed", errors);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleConflict(ConflictException ex) {
        return new ErrorResponse(409, ex.getMessage());
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse handleUnauthorized(RuntimeException ex) {
        return new ErrorResponse(401, "Invalid credentials");
    }

    @ExceptionHandler(SubscriptionRequiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse handleSubscriptionRequired(SubscriptionRequiredException ex) {
        return new ErrorResponse(403, "SUBSCRIPTION_REQUIRED", ex.getMessage());
    }

    @ExceptionHandler(QuotaExceededException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse handleQuotaExceeded(QuotaExceededException ex) {
        return new ErrorResponse(403, "QUOTA_EXCEEDED", ex.getMessage());
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse handleRoleNotAllowed(RoleNotAllowedException ex) {
        return new ErrorResponse(403, "ROLE_NOT_ALLOWED", ex.getMessage());
    }

    @ExceptionHandler(WebhookSignatureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleWebhookSignature(WebhookSignatureException ex) {
        return new ErrorResponse(400, "WEBHOOK_SIGNATURE_INVALID", ex.getMessage());
    }

    // Client-side billing problem (bad plan, payment method not confirmed, …).
    @ExceptionHandler(BillingValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    ErrorResponse handleBillingValidation(BillingValidationException ex) {
        return new ErrorResponse(422, "BILLING_INVALID", ex.getMessage());
    }

    // Stripe is not configured — billing is temporarily unavailable, not a client error.
    @ExceptionHandler(StripeNotConfiguredException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ErrorResponse handleStripeNotConfigured(StripeNotConfiguredException ex) {
        log.error("Billing unavailable: {}", ex.getMessage());
        return new ErrorResponse(503, "BILLING_UNAVAILABLE", ex.getMessage());
    }

    // Genuine upstream Stripe failure (wrapped StripeException). The raw Stripe message stays in the
    // logs; the client only gets a generic message to avoid leaking internal/billing-provider detail.
    @ExceptionHandler(BillingException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    ErrorResponse handleBilling(BillingException ex) {
        log.error("Upstream billing failure: {}", ex.getMessage(), ex);
        return new ErrorResponse(502, "BILLING_ERROR",
                "Le service de paiement est momentanément indisponible. Veuillez réessayer plus tard.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        String message = environment.matchesProfiles("local")
                ? ex.getClass().getSimpleName() + ": " + ex.getMessage()
                : "An unexpected error occurred";
        return new ErrorResponse(500, message);
    }
}
