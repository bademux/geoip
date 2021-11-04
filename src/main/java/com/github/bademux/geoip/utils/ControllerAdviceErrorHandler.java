package com.github.bademux.geoip.utils;

import com.github.bademux.geoip.api.dto.ErrorApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdviceErrorHandler extends ResponseEntityExceptionHandler {

    private final Clock clock;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        UUID errorId = UUID.randomUUID();
        var error = new ErrorApiDto().id(errorId).timestamp(clock.instant().atOffset(UTC));
        log.error("Handling internal error with id {}", errorId, ex);
        if (status.is5xxServerError()) {
            error.code(String.valueOf(INTERNAL_SERVER_ERROR.value())).message(INTERNAL_SERVER_ERROR.toString());
        } else {
            error.code(Integer.toString(status.value())).message(ex.getMessage());
        }
        return super.handleExceptionInternal(ex, error, headers, status, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknown(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, INTERNAL_SERVER_ERROR, request);
    }

}
