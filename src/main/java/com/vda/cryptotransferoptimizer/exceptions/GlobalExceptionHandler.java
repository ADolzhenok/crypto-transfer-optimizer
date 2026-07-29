package com.vda.cryptotransferoptimizer.exceptions;

import com.vda.cryptotransferoptimizer.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CoinNotFoundException.class,
            PairNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleUrlInvalid(RuntimeException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage()
                ));
    }

}
