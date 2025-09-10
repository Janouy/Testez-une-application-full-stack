package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

@SpringBootTest(classes = AuthControllerIT.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthControllerIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "com.openclassrooms.starterjwt")
	static class TestApp {
	}

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private JwtUtils jwtUtils;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@MockBean
	private UserRepository userRepository;

	@Test
	void login_ok_returnsJwtResponse_withAdminTrue() throws Exception {
		// -- Arrange
		Authentication auth = Mockito.mock(Authentication.class);
		UserDetailsImpl principal = Mockito.mock(UserDetailsImpl.class);

		Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(auth);
		Mockito.when(auth.getPrincipal()).thenReturn(principal);

		Mockito.when(jwtUtils.generateJwtToken(auth)).thenReturn("fake-jwt");
		Mockito.when(principal.getId()).thenReturn(1L);
		Mockito.when(principal.getUsername()).thenReturn("alice@example.com");
		Mockito.when(principal.getFirstName()).thenReturn("Alice");
		Mockito.when(principal.getLastName()).thenReturn("Doe");

		User dbUser = new User().setId(1L).setEmail("alice@example.com").setFirstName("Alice").setLastName("Doe")
				.setPassword("hash").setAdmin(true);
		Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(dbUser));

		String body = "{\n" + "  \"email\": \"new@example.com\",\n" + "  \"firstName\": \"Nina\",\n"
				+ "  \"lastName\": \"Jones\",\n" + "  \"password\": \"clearpwd\"\n" + "}";

		mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(body)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.username").value("alice@example.com"))
				.andExpect(jsonPath("$.firstName").value("Alice")).andExpect(jsonPath("$.lastName").value("Doe"))
				.andExpect(jsonPath("$.admin").value(true));
	}

	@Test
	void login_ok_returnsJwtResponse_withAdminFalse_whenUserNotFound() throws Exception {
		Authentication auth = Mockito.mock(Authentication.class);
		UserDetailsImpl principal = Mockito.mock(UserDetailsImpl.class);

		Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(auth);
		Mockito.when(auth.getPrincipal()).thenReturn(principal);

		Mockito.when(jwtUtils.generateJwtToken(auth)).thenReturn("fake-jwt");
		Mockito.when(principal.getId()).thenReturn(2L);
		Mockito.when(principal.getUsername()).thenReturn("bob@example.com");
		Mockito.when(principal.getFirstName()).thenReturn("Bob");
		Mockito.when(principal.getLastName()).thenReturn("Smith");
		Mockito.when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.empty());

		String body = "{\n" + "  \"email\": \"new@example.com\",\n" + "  \"password\": \"clearpwd\"\n" + "}";

		mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(body)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.id").value(2)).andExpect(jsonPath("$.username").value("bob@example.com"))
				.andExpect(jsonPath("$.firstName").value("Bob")).andExpect(jsonPath("$.lastName").value("Smith"))
				.andExpect(jsonPath("$.admin").value(false));
	}

	@Test
	void register_conflict_whenEmailTaken_returns400_withMessage() throws Exception {
		Mockito.when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

		String body = "{\n" + "  \"email\": \"taken@example.com\",\n" + "  \"firstName\": \"Nina\",\n"
				+ "  \"lastName\": \"Jones\",\n" + "  \"password\": \"clearpwd\"\n" + "}";

		mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.message").value("Error: Email is already taken!"));

		verify(userRepository, never()).save(any());
	}

	@Test
	void register_ok_persistsUser_withHashedPassword_andReturns200() throws Exception {
		Mockito.when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
		Mockito.when(passwordEncoder.encode("clearpwd")).thenReturn("hashedpwd");
		Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		String body = "{\n" + "  \"email\": \"new@example.com\",\n" + "  \"firstName\": \"Nina\",\n"
				+ "  \"lastName\": \"Jones\",\n" + "  \"password\": \"clearpwd\"\n" + "}";

		mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.message").value("User registered successfully!"));

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();

		assertThat(saved.getEmail()).isEqualTo("new@example.com");
		assertThat(saved.getFirstName()).isEqualTo("Nina");
		assertThat(saved.getLastName()).isEqualTo("Jones");
		assertThat(saved.isAdmin()).isFalse();
		assertThat(saved.getPassword()).isEqualTo("hashedpwd");
	}

	@Test
	void login_badRequest_whenMissingEmail() throws Exception {
		String body = "{ \"password\": \"x\" }";

		mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(body)).andDo(print())
				.andExpect(status().isBadRequest());

		verify(authenticationManager, never()).authenticate(any());
	}

	@Test
	void register_badRequest_whenInvalidEmailFormat() throws Exception {
		String body = "{\n" + "  \"email\": \"bad-email\",\n" + "  \"firstName\": \"Nina\",\n"
				+ "  \"lastName\": \"Jones\",\n" + "  \"password\": \"clearpwd\"\n" + "}";

		mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isBadRequest());

		verify(userRepository, never()).existsByEmail(any());
		verify(userRepository, never()).save(any());
	}

	@Test
	void register_badRequest_whenMissingPassword() throws Exception {
		String body = "{\n" + "  \"email\": \"nina@example.com\",\n" + "  \"firstName\": \"Nina\",\n"
				+ "  \"lastName\": \"Jones\"\n" + "}";

		mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isBadRequest());

		verify(userRepository, never()).existsByEmail(any());
		verify(userRepository, never()).save(any());
	}

}
