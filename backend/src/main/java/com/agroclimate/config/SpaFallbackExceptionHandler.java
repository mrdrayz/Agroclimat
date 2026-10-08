package com.agroclimate.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * При F5 на клиентских маршрутах Angular ({@code /dashboard}, {@code /warnings}, …) Spring ищет
 * статический файл по этому пути и бросает {@link NoResourceFoundException}. Общий
 * {@link com.agroclimate.exception.GlobalExceptionHandler} перехватывал её как {@link Exception}
 * и отдавал 500 с JSON вместо SPA. Здесь для не-API путей делаем forward на {@code index.html}.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaFallbackExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", ex.getMessage());
            body.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }
        return new ModelAndView("forward:/index.html");
    }
}
