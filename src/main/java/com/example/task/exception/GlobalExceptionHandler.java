    package com.example.task.exception;

    import com.example.task.dto.ErrorResponse;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.dao.DataAccessException;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ExceptionHandler;

    import java.time.LocalDateTime;
    import java.util.Collections;
    import java.util.List;
    import java.util.stream.Collectors;

    @ControllerAdvice
    public class GlobalExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
        @ExceptionHandler(ConfigFileNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleConfigFileNotFoundException(ConfigFileNotFoundException ex) {
            List<String> errorDetails = Collections.singletonList(ex.getMessage());

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Application configuration error",
                    errorDetails,
                    LocalDateTime.now()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }

    @ExceptionHandler({InvalidEmailException.class, InvalidPasswordException.class, InvalidRoleException.class})
        public ResponseEntity<String> handleValidationExceptions(RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<String> handleEmailExistsException(EmailAlreadyExistsException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
        }

        @ExceptionHandler(TaskNotFoundException.class)
        public ResponseEntity<String> handleTaskNotFoundException(TaskNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler({UnauthorizedActionException.class, AccessDeniedException.class})
        public ResponseEntity<String> handleAuthorizationExceptions(RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<String> handleInvalidRequestException(InvalidRequestException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
            logger.error("Database error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error: " + ex.getLocalizedMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
            String errorMessage = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleGeneralException(Exception ex) {
            logger.error("Unhandled exception: ", ex);
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }