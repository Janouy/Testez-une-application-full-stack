package com.openclassrooms.starterjwt.webSecurity;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
class WebSecurityConfigTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	UserDetailsServiceImpl userDetailsService;

	@MockBean
	JwtUtils jwtUtils;

	private static final String PROTECTED_URL = "/api/session";
	private static final String USERNAME = "user@test.com";
	private static final String BEARER = "Bearer faketoken";

	@Test
	void givenValidJwt_whenAccessProtected_then200_and_UserDetailsServiceCalled() throws Exception {
		when(jwtUtils.validateJwtToken("faketoken")).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken("faketoken")).thenReturn(USERNAME);

		UserDetails ud = new User(USERNAME, "ignored", List.of(new SimpleGrantedAuthority("ROLE_USER")));
		when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(ud);

		mockMvc.perform(get(PROTECTED_URL).header("Authorization", BEARER)).andExpect(status().isOk());

		verify(userDetailsService, times(1)).loadUserByUsername(USERNAME);

		verifyNoMoreInteractions(userDetailsService);
	}

	@Test
	void givenNoJwt_whenAccessProtected_then401_and_UserDetailsServiceNotCalled() throws Exception {
		mockMvc.perform(get(PROTECTED_URL)).andExpect(status().isUnauthorized()).andExpect(unauthenticated());

		verifyNoInteractions(userDetailsService);
	}

	@Test
	void givenInvalidJwt_whenAccessProtected_then401_and_UserDetailsServiceNotCalled() throws Exception {

		when(jwtUtils.validateJwtToken("faketoken")).thenReturn(false);

		mockMvc.perform(get(PROTECTED_URL).header("Authorization", BEARER)).andExpect(status().isUnauthorized())
				.andExpect(unauthenticated());

		verifyNoInteractions(userDetailsService);
	}
}
