package com.example.api_translation.domain.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
@Log4j2
public class GlobalHandleException extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponse> globalExceptionHandler(
          Exception ex, WebRequest request) {
    String msg = "-ERROR : " + ex.getMessage();

    if (ex instanceof BusinessException) {

      BusinessException businessException = (BusinessException) ex;
      ExceptionResponse exceptionResponse =
              new ExceptionResponse(
                      businessException.status,
                      new Date(),
                      businessException.getMessage(),
                      ex.getMessage(),
                      ((ServletWebRequest) request).getRequest().getServletPath());
      return new ResponseEntity<>(exceptionResponse, businessException.status);
    }
    ExceptionResponse exceptionResponse =
            new ExceptionResponse(
                    INTERNAL_SERVER_ERROR,
                    new Date(),
                    "Đã có lỗi xảy ra.",
                    ex.getMessage(),
                    ((ServletWebRequest) request).getRequest().getServletPath());
    return new ResponseEntity<>(exceptionResponse, INTERNAL_SERVER_ERROR);
  }

}
