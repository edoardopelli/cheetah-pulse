// security/JwtAuthenticationFilter.java
package org.cheetah.pulse.security;

import java.io.IOException;
import java.util.List;

import org.cheetah.pulse.repo.UserAccountRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserAccountRepository userRepo;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				Claims claims = jwtService.parseClaims(token);
				String userId = claims.getSubject();
				var user = userRepo.findById(userId).orElse(null);
				if (user != null && user.isEnabled()) {
					var authToken = new UsernamePasswordAuthenticationToken(user.getId(), null,
							List.of(new SimpleGrantedAuthority("ROLE_USER")));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			} catch (Exception e) {
				log.debug("JWT invalid: {}", e.getMessage());
			}
		}
		chain.doFilter(request, response);
	}
}