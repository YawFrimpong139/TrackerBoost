package org.codewithzea.trackerboost.exception;


import org.codewithzea.trackerboost.audit.SecurityAuditService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SecurityAuditService securityAuditService;

    @Value("${app.environment:prod}")
    private String environment;
    private boolean includeStackTrace;

    @PostConstruct
    public void init() {
        this.includeStackTrace = "dev".equals(environment);
    }

    // ========== Business Exceptions ==========
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request,
                null,
                false
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = Optional.of(ex.getBindingResult())
                .map(bindingResult -> bindingResult.getFieldErrors()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                fieldError -> fieldError.getDefaultMessage() != null ?
                                        fieldError.getDefaultMessage() : "No error message",
                                (existing, replacement) -> existing
                        )))
                .orElseGet(Collections::emptyMap);

        log.warn("Validation errors: {}", errors);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Invalid request content",
                request,
                Map.of("errors", errors),
                false
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("Type mismatch: {}", error);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "TYPE_MISMATCH",
                error,
                request,
                null,
                false
        );
    }

    // ========== Security Exceptions ==========
    @ExceptionHandler({
            BadCredentialsException.class,
            InsufficientAuthenticationException.class,
            AuthenticationServiceException.class,
            DisabledException.class,
            LockedException.class,
            AccountExpiredException.class,
            CredentialsExpiredException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(AuthenticationException ex, WebRequest request) {
        String username = getUsername(request);
        String ipAddress = getClientIp();

        securityAuditService.logLoginFailure(username, ipAddress, ex.getMessage());
        log.warn("Authentication failed for user {}: {}", username, ex.getMessage());

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                ex.getMessage(),
                request,
                null,
                false
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        String username = getUsername(request);
        String ipAddress = getClientIp();

        securityAuditService.logUnauthorizedAccess(
                "ACCESS_DENIED",
                ipAddress,
                ex.getMessage()
        );

        log.warn("Access denied for user {}: {}", username, ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                request,
                null,
                false
        );
    }

    // ========== JWT Exceptions ==========
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex, WebRequest request) {
        log.warn("JWT token expired: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "TOKEN_EXPIRED",
                "Authentication token has expired",
                request,
                Map.of("expiredAt", ex.getClaims().getExpiration()),
                false
        );
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class, UnsupportedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtExceptions(JwtException ex, WebRequest request) {
        log.warn("JWT error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "INVALID_TOKEN",
                "Invalid authentication token",
                request,
                null,
                false
        );
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtValidation(JwtValidationException ex, WebRequest request) {
        List<String> errors = ex.getErrors().stream()
                .map(error -> String.format("%s: %s", error.getErrorCode(), error.getDescription()))
                .collect(Collectors.toList());

        log.warn("JWT validation failed: {}", errors);
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "TOKEN_VALIDATION_FAILED",
                "Invalid authentication token",
                request,
                Map.of("validationErrors", errors),
                false
        );
    }

    // ========== OAuth2 Exceptions ==========
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2Authentication(OAuth2AuthenticationException ex, WebRequest request) {
        OAuth2Error error = ex.getError();
        log.warn("OAuth2 error: {}", error.getDescription());

        return buildErrorResponse(
                HttpStatus.valueOf(error.getErrorCode()),
                "OAUTH2_ERROR",
                error.getDescription(),
                request,
                Map.of("errorUri", error.getUri()),
                false
        );
    }

    // ========== Fallback Exception Handler ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request,
                null,
                includeStackTrace
        );
    }

    // ========== Helper Methods ==========
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            WebRequest request
    ) {
        return buildErrorResponse(status, code, message, request, null, false);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            WebRequest request,
            Map<String, Object> details
    ) {
        return buildErrorResponse(status, code, message, request, details, false);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            WebRequest request,
            Map<String, Object> details,
            boolean includeStackTrace
    ) {
        String path = getRequestPath(request);
        List<String> stackTrace = includeStackTrace ?
                getLimitedStackTrace() : null;

        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        status.value(),
                        code,
                        message,
                        Instant.now(),
                        path,
                        details,
                        stackTrace
                )
        );
    }

    private List<String> getLimitedStackTrace() {
        return Stream.of(Thread.currentThread().getStackTrace())
                .limit(10)
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return "unknown";
    }

    private String getClientIp() {
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");
            return (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress))
                    ? request.getRemoteAddr()
                    : ipAddress;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUsername(WebRequest request) {
        try {
            return request.getUserPrincipal() != null
                    ? request.getUserPrincipal().getName()
                    : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }
}