package com.marcus.securebusiness.util;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcus.securebusiness.exception.ApiException;
import com.marcus.securebusiness.model.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
public class ExceptionUtils {
    public static void processError (HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof ApiException || exception instanceof DisabledException || exception instanceof LockedException ||
                exception instanceof InvalidClaimException || exception instanceof BadCredentialsException) {
            HttpResponse httpResponse = getHttpRequest(response, exception.getMessage(), BAD_REQUEST);
            writeResponse(response, httpResponse);
        } else if (exception instanceof TokenExpiredException) {
            HttpResponse httpResponse = getHttpRequest(response, "Authorization Error", UNAUTHORIZED);
            writeResponse(response, httpResponse);
        } else {
            HttpResponse httpResponse = getHttpRequest(response, "An error occur at "+ request.getRequestURI(), INTERNAL_SERVER_ERROR);
            writeResponse(response, httpResponse);
        }
        log.error("Exception occur at "+ request.getRequestURI() + " " + exception.getMessage());
    }

    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        try {
            OutputStream out = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, httpResponse);
            out.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static HttpResponse getHttpRequest(HttpServletResponse response, String message, HttpStatus httpStatus) {
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(message)
                .status(httpStatus)
                .statusCode(httpStatus.value())
                .build();
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        return httpResponse;
    }
}
