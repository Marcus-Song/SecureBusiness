package com.marcus.securebusiness.exception;

import com.marcus.securebusiness.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.resolve;

@RestControllerAdvice
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Internal Server Error")
                        .developerMessage(exception.getMessage())
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        String fieldMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(fieldMessage)
                        .developerMessage(exception.getMessage())
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> sQLIntegrityConstraintViolationException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Internal Database Error")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException(BadCredentialsException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Incorrect email or password")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> apiException(ApiException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Access denied! You dont have access!")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("expected 1, actual 0") ? "Record not found" : "Some error occur")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> disabledException(DisabledException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Please check your email and verify your account")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("expected 1, actual 0") ? "Record not found" : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> lockedException(LockedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<> (
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Too many fail attempts!")
                        .developerMessage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }
}
