package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserRepository userRepository;

	@Mock
	private Authentication authentication;

	@Mock
	private UserDetailsImpl userDetails;
	private AuthController authController;

	@BeforeEach
	void setUp() {
		authController = new AuthController(authenticationManager, passwordEncoder, jwtUtils, userRepository);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticateUser_success_adminTrue_returnsJwtResponse() {
		// GIVEN
		LoginRequest req = new LoginRequest();
		req.setEmail("admin@example.com");
		req.setPassword("secret");

		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(userDetails);

		when(userDetails.getId()).thenReturn(42L);
		when(userDetails.getUsername()).thenReturn("admin@example.com");
		when(userDetails.getFirstName()).thenReturn("Alice");
		when(userDetails.getLastName()).thenReturn("Wonder");
		when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

		User dbUser = new User("admin@example.com", "Wonder", "Alice", "hashed", true);
		when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(dbUser));

		ResponseEntity<?> response = authController.authenticateUser(req);

		// THEN
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody()).isInstanceOf(JwtResponse.class);

		JwtResponse body = (JwtResponse) response.getBody();
		assertThat(body.getToken()).isEqualTo("jwt-token");
		assertThat(body.getId()).isEqualTo(42L);
		assertThat(body.getUsername()).isEqualTo("admin@example.com");
		assertThat(body.getFirstName()).isEqualTo("Alice");
		assertThat(body.getLastName()).isEqualTo("Wonder");

	}

	@Test
	void authenticateUser_whenUserNotFound_defaultsAdminFalse() {
		// GIVEN
		LoginRequest req = new LoginRequest();
		req.setEmail("mail@example.com");
		req.setPassword("secret");

		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getId()).thenReturn(99L);
		when(userDetails.getUsername()).thenReturn("mail@example.com");
		when(userDetails.getFirstName()).thenReturn("Alice");
		when(userDetails.getLastName()).thenReturn("Wonder");
		when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

		when(userRepository.findByEmail("mail@example.com")).thenReturn(Optional.empty());

		// WHEN
		ResponseEntity<?> response = authController.authenticateUser(req);

		// THEN
		JwtResponse body = (JwtResponse) response.getBody();
		assertThat(body.getAdmin()).isFalse();
	}

	@Test
	void registerUser_whenEmailAlreadyExists_returnsBadRequestWithMessage() {
		// GIVEN
		SignupRequest req = new SignupRequest();
		req.setEmail("mail@example.com");
		req.setFirstName("Alice");
		req.setLastName("Wonder");
		req.setPassword("secret");

		when(userRepository.existsByEmail("mail@example.com")).thenReturn(true);

		// WHEN
		ResponseEntity<?> response = authController.registerUser(req);

		// THEN
		assertThat(response.getStatusCodeValue()).isEqualTo(400);
		assertThat(response.getBody()).isInstanceOf(MessageResponse.class);

		MessageResponse body = (MessageResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Error: Email is already taken!");
	}
}
