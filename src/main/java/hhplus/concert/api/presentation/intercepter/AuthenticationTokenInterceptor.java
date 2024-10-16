package hhplus.concert.api.presentation.intercepter;

import hhplus.concert.support.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationTokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthenticationTokenInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().startsWith("/queue")) {

            String token = request.getHeader("Authorization");
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
        return true;
    }

    /**
     * 토큰을 검증하는 로직
     */
    private boolean isValidToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
