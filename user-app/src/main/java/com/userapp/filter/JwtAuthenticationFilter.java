package com.userapp.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userapp.dto.response.ErrorResponseDto;
import com.userapp.entity.User;
import com.userapp.exception.DeactivatedUserException;
import com.userapp.service.CustomUserDetailsService;
import com.userapp.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;

import static com.userapp.constants.ConstantStrings.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService,
                                 ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                throw new JwtException(JWT_TOKEN_MISSING);
            }

            if (jwtUtil.validateToken(jwt)) {
                String userEmail = jwtUtil.getEmailFromToken(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                // Проверяем статус пользователя
                if (userDetails instanceof User user && !user.isActive()) {
                    throw new DeactivatedUserException(USER_ACCOUNT_DEACTIVATED);
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, JWT_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, JWT_TOKEN_INVALID);
        } catch (DeactivatedUserException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (JwtException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during authentication.");
        }
    }

    private boolean isPublicEndpoint(String path, String method) {
        return ("/api/user/login".equals(path) && "POST".equalsIgnoreCase(method)) ||
               ("/api/user/register".equals(path) && "POST".equalsIgnoreCase(method)) ||
               ("/api/user/refresh".equals(path) && "POST".equalsIgnoreCase(method)) ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               "/swagger-ui.html".equals(path) ||
               path.startsWith("/error");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDto errorResponse = new ErrorResponseDto(message);

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, errorResponse);
        out.flush();
    }
}