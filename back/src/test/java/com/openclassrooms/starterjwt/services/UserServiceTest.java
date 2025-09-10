package com.openclassrooms.starterjwt.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void delete_shouldDelegateToRepository() {
		// GIVEN
		Long id = 42L;

		// WHEN
		userService.delete(id);

		// THEN
		verify(userRepository).deleteById(id);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	void findById_shouldReturnUser_whenFound() {
		// GIVEN
		Long id = 1L;
		User user = new User();
		user.setId(id);
		user.setEmail("alice@example.com");
		when(userRepository.findById(id)).thenReturn(Optional.of(user));

		// WHEN
		User result = userService.findById(id);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getEmail()).isEqualTo("alice@example.com");
		verify(userRepository).findById(id);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	void findById_shouldReturnNull_whenNotFound() {
		// GIVEN
		Long id = 999L;
		when(userRepository.findById(id)).thenReturn(Optional.empty());

		// WHEN
		User result = userService.findById(id);

		// THEN
		assertThat(result).isNull();
		verify(userRepository).findById(id);
		verifyNoMoreInteractions(userRepository);
	}
}
