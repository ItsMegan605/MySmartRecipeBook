package it.unipi.MySmartRecipeBook.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//tipi di errori: 100 informativo, 200 tutto ok, 300 reindirizzamento, 400 errore client
//500 errore server
//global handler where the different exceptions for the application are handler


/* SWAGGER: annotation for documentation, ci va messo mediatype, in che modo è la ripssita 
* schema: tipo di struttura dati restituita */

/**
 * MethodArgumentNotValidException: se per esempio ho un not blank e lo lascio vuoto ***
 * così non mi da tutto lo stack trace
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occured",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
            )
    })
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleUserLoginException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> { //prende tutti gli errori e li analizza
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }


    /**
     * ConstraintViolationException: fallimento sui parametri URL, tipo età e io metto sbagliata
     * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Constraint violeted",
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
 * TypeMismatchException: Se non converte parametri, es sbaglio endpoint API
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
     * Exception: intercetta qualsiasi errore non previsto, mi da errore generico
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
        return ResponseEntity.internalServerError().body("Internal Server Error occured while using the Application");
    }

    /**
     *  IllegalArgumentException: parametro ok, ma non logicamente accettabile dalla logica
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
        return ResponseEntity.badRequest().body("Illegal argument");
    }

    /**
     *  NoSuchElementException: quando chiamo qualcosa ceh non esiste
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("resource not found");
    }

/**
 * AccessDeniedException: generata da sping security, quando vuole fa op per cu non ha p4rmesso
 * */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Action",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            )
    })
    //tentativo di accesso a una risorsa protetta senza i permessi necessari
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + ex.getMessage());
    }


/**
 *  HttpMessageNotReadableException: quando manda JSON fatto male
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
        return ResponseEntity.badRequest().body("HTTP request is not readable: " + ex.getMessage());
    }


}