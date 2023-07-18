package com.marcus.securebusiness.filter;

import com.marcus.securebusiness.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.marcus.securebusiness.util.ExceptionUtils.processError;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";
    private static final String[] PUBLIC_ROUTES = {"/user/register", "/user/login", "/user/verify/code",
            "/user/refresh/token", "user/image", "/user/new/password", "/user/resetpassword1" };
    protected static final String EMAIL_KEY = "email";
    protected static final String TOKEN_KEY = "token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //if (shouldNotFilter(request)) filterChain.doFilter(request, response);
            if (request.getRequestURI().contains("/user/resetpassword1")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = getToken(request);
            Long userId = getUserId(request);
            if (tokenProvider.isTokenValid(userId, token)) {
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
                Authentication authentication = tokenProvider.getAuthentication(userId, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Token is valid");
            } else {
                log.info("Token is NOT valid");
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            processError(request, response, exception);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(AUTHORIZATION) == null ||
                !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD) ||
                asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

    private Long getUserId(HttpServletRequest request) {
        return tokenProvider.getSubject(getToken(request), request);
    }

    private String getToken(HttpServletRequest request) {
        return ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, EMPTY)).get();
    }
}
