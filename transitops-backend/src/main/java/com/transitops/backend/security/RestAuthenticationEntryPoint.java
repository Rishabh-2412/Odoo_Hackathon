package com.transitops.backend.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitops.backend.common.error.ApiError;
import com.transitops.backend.common.web.RequestIdFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Object requestId = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        ApiError error = new ApiError(
                Instant.now(), 401, "Unauthorized", "AUTHENTICATION_REQUIRED",
                "A valid access token is required", request.getRequestURI(),
                requestId == null ? null : requestId.toString(), Map.of());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
