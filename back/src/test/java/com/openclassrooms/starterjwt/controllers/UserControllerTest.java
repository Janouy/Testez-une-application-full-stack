package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private UserMapper userMapper;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@Mock
	private UserDetails principal;

	private UserController controller;

	@BeforeEach
	void setUp() {
		controller = new UserController(userService, userMapper);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void findById_whenValidIdAndFound_returns200WithDto() {
		User entity = new User("john@example.com", "Doe", "John", "pwd", false);
		UserDto dto = new UserDto();

		when(userService.findById(10L)).thenReturn(entity);
		when(userMapper.toDto(entity)).thenReturn(dto);

		ResponseEntity<?> resp = controller.findById("10");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(dto);
	}

	@Test
	void findById_whenValidIdAndNotFound_returns404() {
		when(userService.findById(404L)).thenReturn(null);

		ResponseEntity<?> resp = controller.findById("404");

		assertThat(resp.getStatusCodeValue()).isEqualTo(404);
		assertThat(resp.getBody()).isNull();
	}

	@Test
	void findById_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.findById("abc");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verifyNoInteractions(userMapper);
	}

	@Test
	void delete_whenAuthorizedAndUserExists_returns200_andCallsDelete() {

		User entity = new User("alice@example.com", "Doe", "Alice", "pwd", false);
		when(userService.findById(7L)).thenReturn(entity);

		when(principal.getUsername()).thenReturn("alice@example.com");
		when(authentication.getPrincipal()).thenReturn(principal);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		ResponseEntity<?> resp = controller.save("7");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		verify(userService).delete(7L);
	}

	@Test
	void delete_whenUserExistsButUnauthorized_returns401_andDoesNotDelete() {
		User entity = new User("bob@example.com", "Doe", "Bob", "pwd", false);
		when(userService.findById(8L)).thenReturn(entity);

		when(principal.getUsername()).thenReturn("other@example.com");
		when(authentication.getPrincipal()).thenReturn(principal);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		ResponseEntity<?> resp = controller.save("8");

		assertThat(resp.getStatusCodeValue()).isEqualTo(401);
		verify(userService, never()).delete(anyLong());
	}

	@Test
	void delete_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.save("bad");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verify(userService, never()).delete(anyLong());
	}
}
