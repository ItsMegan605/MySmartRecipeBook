package it.unipi.MySmartRecipeBook.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global handler for the different type of exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * MethodArgumentNotValidException: Exception that occurs when a request payload fails @Valid validation
     * for example missing required fields or empty strings in a DTO.
     * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occurred",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
            )
    })
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> { //Extracts and analyzes all field errors
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }


    /**
     * ConstraintViolationException: when validation fails directly on method parameters
     * for example an invalid age.
     * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Constraint violated",
                    content = @Content( mediaType = "application/json", schema = @Schema(implementation = Map.class))
            )
    })
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach((error) -> {
            String fieldName = error.getPropertyPath().toString();
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
/**
 * TypeMismatchException: thrown when a request parameter or path variable cannot be
 * converted to the expected Java type
 * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Mismatch with the field",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatchException(TypeMismatchException e) {
        return  ResponseEntity.badRequest().body("invalid field");

    }

    /**
     * Exception: global  handler for any unexpected Exception
     * not caught by specific handlers.
     * */

    @ApiResponses({
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(cause.getMessage());
            }
            cause = cause.getCause();
        }
        return ResponseEntity.internalServerError().body("Internal Server Error occurred while using the application");
    } //TODO: decidere cosa vogliamo fare perchè così stampa solo il mess ma su java non vedo nulla

    /**
     *  IllegalArgumentException: illegal argument inserted
     *  */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Illegal argument",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage()); //Returns the custom message defined when the exception was thrown
    }

    /**
     *  NoSuchElementException: when something doesn't exist and/or can't be found
     *  */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "Error 404 not found.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

/**
 * AccessDeniedException: This occurs when an authenticated user attempts to execute an operation or access
 * an endpoint without the required roles or permissions.
 * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Action",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: you don't have rights for this operation");
    }


/**
 *  HttpMessageNotReadableException: Exception that handles HTTP request body:  malformed or unreadable
 *  */
@ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Wrong request",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("HTTP request is not readable: format error");
    }

    /**
     * Method to handle the data integrity
     * @param ex - name of the exception
     * @return the message of the exception
     */

    @ApiResponses({
            @ApiResponse(
                    responseCode = "409",
                    description = "Data Integrity Violation / Conflict",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Exception for the user's username while logging in the application
     * @param unf - exception name
     * @return The message
     */


    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Wrong credentials",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUserNotfoundException(UsernameNotFoundException unf) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unf.getMessage());
    }

    /**
     * Exception to handle wrong credentials in the application
     * @param bce - Name of the exception
     * @return The message
     */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Wrong credentials",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException bce){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username or password are not valid");
    }


}