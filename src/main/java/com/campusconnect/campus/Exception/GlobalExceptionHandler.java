package com.campusconnect.campus.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //  JSON FORMAT BODY -.FOR CLEARER RESPONSE OF ENTITY
    private ResponseEntity<Map<String,Object>> buildResponse(HttpStatus status, String message){
        Map<String,Object> map=new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        map.put("status",status.value()); // 401 etc
        map.put("error",status.getReasonPhrase()); // reason in string format "Not found"
        map.put("message",message);
        return ResponseEntity.status(status).body(map);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotFoundException(UserNotFoundException e) {
        return  buildResponse(HttpStatus.NOT_FOUND,e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException e) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeNotFoundException(
            EmployeeNotFoundException e) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccessException(
            UnauthorizedAccessException e) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(
            BadRequestException e) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }
    @ExceptionHandler(EmailAlreadyExist.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExist(
            EmailAlreadyExist e) {

        return buildResponse(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }


    @ExceptionHandler(AlreadyOwnerException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyOwnerException(
            AlreadyOwnerException e) {

        return buildResponse(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return  buildResponse(HttpStatus.UNAUTHORIZED,e.getMessage());
    }
    @ExceptionHandler(TimeOutException.class)
    public ResponseEntity<Map<String,Object>> handleTimeOutException(TimeOutException e) {
        return  buildResponse(HttpStatus.BAD_REQUEST,e.getMessage());
    }
    @ExceptionHandler(AlreadyVerifiedUserException.class)
    public ResponseEntity<Map<String,Object>> handleAlreadyVerifiedUserException(AlreadyVerifiedUserException e) {
        return  buildResponse(HttpStatus.CONFLICT,e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }
}
