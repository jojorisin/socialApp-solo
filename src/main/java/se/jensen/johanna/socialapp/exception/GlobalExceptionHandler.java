package se.jensen.johanna.socialapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import se.jensen.johanna.socialapp.dto.ErrorResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource.",
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(), request);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(RefreshTokenException e, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "REFRESH_TOKEN_EXPIRED", e.getMessage(), request);
    }


    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e,
                                                              WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                e.getMessage(),
                request);

    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException e, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED_ACCESS",
                e.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalFriendshipStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalFriendshipState(
            IllegalFriendshipStateException e, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "ILLEGAL_FRIENDSHIP_STATE",
                e.getMessage(),
                request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), request);

    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthentication(
            JwtAuthenticationException e, WebRequest request) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED, "JWT_AUTHENTICATION", e.getMessage(), request);
    }

    @ExceptionHandler(NotUniqueException.class)
    public ResponseEntity<ErrorResponse> handleNotUnique(NotUniqueException e, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "NOT_UNIQUE", e.getMessage(), request);
    }

    @ExceptionHandler(PasswordMisMatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMisMatch(PasswordMisMatchException e, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errors.put(fieldName, message);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e, WebRequest request) {
        log.error("Unexpected error occurred: ", e); // Logga stacktracet så du kan fixa det
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        String path = getPath(request);
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), error, message, path, Instant.now().toString()));
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
