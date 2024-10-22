package hhplus.concert.api.presentation.intercepter;

import hhplus.concert.support.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class AuthenticationTokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthenticationTokenInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청이 HandlerMethod인지 확인
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            // 메서드 파라미터에 @RequestHeader TOKEN이 있는지 확인
            boolean hasTokenHeader = Arrays.stream(method.getParameters())
                    .anyMatch(parameter -> parameter.isAnnotationPresent(RequestHeader.class) &&
                            "TOKEN".equals(parameter.getAnnotation(RequestHeader.class).value()));

            if (hasTokenHeader) {
                String token = request.getHeader("TOKEN"); // "Authorization" 대신 "TOKEN"으로 변경
                // 토큰이 유효하지 않으면 401 Unauthorized 에러 응답
                if (token == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token not found");
                    return false;
                }
                if (!isValidToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid token");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 토큰을 검증하는 로직
     */
    private boolean isValidToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
