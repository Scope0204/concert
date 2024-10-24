package hhplus.concert.api.presentation.intercepter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            String method = request.getMethod();
            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
            Map<String, String> headers = new HashMap<>();
            Collections.list(request.getHeaderNames())
                    .forEach(headerName -> headers.put(headerName, request.getHeader(headerName)));
            Map<String, String[]> parameterMap = request.getParameterMap();

            logger.info("Request: [{}] {}{}", method, requestUri, queryString);
            logger.info("Request Headers: {}", headers);
            logger.info("Request Parameters: {}", parameterMap);
        }
        return true;
    }
}
