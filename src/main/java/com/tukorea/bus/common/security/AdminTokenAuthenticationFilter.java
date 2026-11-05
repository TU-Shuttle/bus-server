package com.tukorea.bus.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code X-Admin-Token} 헤더가 설정된 관리자 토큰과 같으면 ROLE_ADMIN 으로 인증한다.
 * 토큰이 설정되지 않았으면(빈 값) 누구도 관리자로 인증되지 않는다.
 */
public class AdminTokenAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Admin-Token";

    private final byte[] expected;

    public AdminTokenAuthenticationFilter(String adminToken) {
        this.expected = adminToken == null ? new byte[0] : adminToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (expected.length > 0 && provided != null
                && MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        }
        chain.doFilter(request, response);
    }
}
