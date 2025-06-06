package com.userapp.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter implements Filter {
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);

            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
} 