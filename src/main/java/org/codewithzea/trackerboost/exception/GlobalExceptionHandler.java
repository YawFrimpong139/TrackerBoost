package org.codewithzea.trackerboost.exception;


import org.codewithzea.trackerboost.audit.SecurityAuditService;
import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SecurityAuditService securityAuditService;

    // ========== Business Exceptions ==========
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request
        );
    }

    // ========== Validation Exceptions ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation errors: {}", errors);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Invalid request parameters",
                request,
                Map.of("errors", errors)
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
                "Type Mismatch",
                error,
                request
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
                "Authentication Failed",
                ex.getMessage(),
                request
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
                "Access Denied",
                "You don't have permission to access this resource",
                request
        );
    }

    // ========== JWT Exceptions ==========
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex, WebRequest request) {
        log.warn("JWT token expired: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Token Expired",
                "Authentication token has expired",
                request,
                Map.of("expiredAt", ex.getClaims().getExpiration())
        );
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class, UnsupportedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtExceptions(JwtException ex, WebRequest request) {
        log.warn("JWT error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid Token",
                "Invalid authentication token",
                request
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
                "Token Validation Failed",
                "Invalid authentication token",
                request,
                Map.of("validationErrors", errors)
        );
    }

    // ========== OAuth2 Exceptions ==========
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2Authentication(OAuth2AuthenticationException ex, WebRequest request) {
        OAuth2Error error = ex.getError();
        log.warn("OAuth2 error: {}", error.getDescription());

        return buildErrorResponse(
                HttpStatus.valueOf(error.getErrorCode()),
                "OAuth2 Error",
                error.getDescription(),
                request,
                Map.of("errorUri", error.getUri())
        );
    }

    // ========== Fallback Exception Handler ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request
        );
    }

    // ========== Helper Methods ==========
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            WebRequest request) {
        return buildErrorResponse(status, error, message, request, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            WebRequest request,
            Map<String, Object> details) {

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                error,
                message,
                Instant.now(),
                request.getDescription(false),
                details
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            return ipAddress;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUsername(WebRequest request) {
        try {
            return request.getUserPrincipal() != null ?
                    request.getUserPrincipal().getName() : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }

    // ========== Response DTO ==========
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private Instant timestamp;
        private String path;
        private Map<String, Object> details;
    }
}
