package com.shepherdmoney.interviewproject.advice;

import com.shepherdmoney.interviewproject.exception.InterviewProjectException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handleInvalidInput(MethodArgumentNotValidException ex){
        Map<String,String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error->
                errorMap.put(error.getField(),error.getDefaultMessage()));
        return errorMap;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InterviewProjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleUserNotFound(InterviewProjectException ex){
        return ex.getMessage();
    }
}
