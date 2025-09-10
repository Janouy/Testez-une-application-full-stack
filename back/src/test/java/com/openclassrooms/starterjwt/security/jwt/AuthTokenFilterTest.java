package com.openclassrooms.starterjwt.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

	@InjectMocks
	private AuthTokenFilter filter;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserDetailsServiceImpl userDetailsService;

	@Mock
	private FilterChain filterChain;

	@BeforeEach
	void init() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_withValidJwt_setsAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("valid.jwt.token")).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken("valid.jwt.token")).thenReturn("user@example.com");
		User userDetails = new User("user@example.com", "pwd", Collections.emptyList());
		when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);

		ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor
				.forClass(UsernamePasswordAuthenticationToken.class);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withInvalidJwt_doesNotSetAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer bad.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateJwtToken("bad.jwt.token")).thenReturn(false);

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtUtils).validateJwtToken("bad.jwt.token");
		verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
		verifyNoInteractions(userDetailsService);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withoutAuthorizationHeader_doesNothing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verifyNoInteractions(jwtUtils, userDetailsService);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withMalformedHeader_doesNothing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "BearerX token"); // pas "Bearer "
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verifyNoInteractions(jwtUtils, userDetailsService);
		verify(filterChain).doFilter(request, response);
	}

}
