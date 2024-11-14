package hhplus.concert.api.presentation.intercepter;

import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.support.type.QueueStatus;
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
public class TokenInterceptor implements HandlerInterceptor {

    private static final String TOKEN = "Token";
    private final JwtUtil jwtUtil;
    private final QueueService queueService;

    public TokenInterceptor(JwtUtil jwtUtil, QueueService queueService) {
        this.jwtUtil = jwtUtil;
        this.queueService = queueService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청이 HandlerMethod 인지 확인
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 메서드 파라미터에 @RequestHeader Token 있는지 확인
            boolean hasTokenHeader = Arrays.stream(method.getParameters())
                    .anyMatch(parameter -> parameter.isAnnotationPresent(RequestHeader.class) &&
                            TOKEN.equals(parameter.getAnnotation(RequestHeader.class).value()));
            if (hasTokenHeader) {
                String token = request.getHeader(TOKEN);

                if (token == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token not found");
                    return false;
                }
                if (!isValidateQueueStatus(token)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    /**
     * 토큰을 통해 대기열 상태와 만료 상태를 확인
     */
    private boolean isValidateQueueStatus(String token) {
        QueueStatus queueStatus = queueService.getQueueStatus(token); // 서비스 계층에서 token 확인
        return queueStatus != null && queueStatus != QueueStatus.EXPIRED;
    }
}
