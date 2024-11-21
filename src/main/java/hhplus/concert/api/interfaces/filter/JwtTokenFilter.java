package hhplus.concert.api.interfaces.filter;

import hhplus.concert.support.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String TOKEN = "Token";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(TOKEN);

        if (token != null && !isValidToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String token) {
        return jwtUtil.validateToken(token);
    }

}
