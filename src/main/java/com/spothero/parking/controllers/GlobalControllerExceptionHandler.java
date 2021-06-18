package com.spothero.parking.controllers;

import com.spothero.parking.dtos.ApiErrorDTO;
import com.spothero.parking.exceptions.InvalidInputException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    // Global Errors
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorDTO unknownException(Exception ex, WebRequest req) {
        log.error("Error", ex);
        return new ApiErrorDTO(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Endpoint not found error
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorDTO noHandlerFoundException(NoHandlerFoundException ex, WebRequest req) {
        return new ApiErrorDTO(ex, HttpStatus.NOT_FOUND);
    }

    // Http Request Errors
    // Http Wrong Media Type Errors
    // Json Malformated Errors
    // No need for logs
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class,
        HttpMediaTypeNotSupportedException.class, HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO badRequestExceptions(Exception ex, WebRequest req) {
        return new ApiErrorDTO(ex, HttpStatus.BAD_REQUEST);
    }

    // Input errors
    // No need for logs
    @ExceptionHandler(value = {InvalidInputException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO inputExceptions(Exception ex, WebRequest req) {
        return new ApiErrorDTO(ex, HttpStatus.BAD_REQUEST);
    }

    // Validation errors
    // No need for logs
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO validationExceptions(MethodArgumentNotValidException ex) {
        return createInvalidInput(ex, HttpStatus.BAD_REQUEST);
    }

    private ApiErrorDTO createInvalidInput(MethodArgumentNotValidException ex, HttpStatus status) {
        List<String> messages = new ArrayList<>();
        for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {

            if (objectError instanceof FieldError) {

                FieldError fieldError = (FieldError) objectError;
                Boolean isBlankMessage = fieldError.getDefaultMessage().toLowerCase().contains("blank")
                        || fieldError.getDefaultMessage().toLowerCase().contains("null")
                        || fieldError.getDefaultMessage().toLowerCase().contains("empty");

                String objectWithField = getObjectWithField(fieldError.getField());
                String fieldValue = fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : "";

                String message = getMessage(fieldError.getField(), objectWithField, fieldError.getDefaultMessage(),
                        isBlankMessage, fieldValue);

                messages.add(message);
            } else {
                messages.add(objectError.getDefaultMessage());
            }

        }

        return new ApiErrorDTO(StringUtils.join(messages, " | "), status);
    }

    private String getMessage(String originalField, String field, String defaultMessage, Boolean isBlankMessage,
            String fieldValue) {
        defaultMessage = isBlankMessage
                ? StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field), " "))
                + " "
                + defaultMessage
                : defaultMessage;

        if (defaultMessage.contains("{value}") && isNotEmpty(fieldValue)) {
            defaultMessage = defaultMessage.replace("{value}", fieldValue);
        }

        return originalField + ": " + defaultMessage;
    }

    private String getObjectWithField(String str) {
        String result = str;
        String field = StringUtils.substringAfterLast(str, ".");
        String object = StringUtils.substringBefore(str, "[");

        if (isNotEmpty(field) && isNotEmpty(object)) {
            result = object.trim() + StringUtils.capitalize(field).trim();
        }

        return result.trim();
    }

}
