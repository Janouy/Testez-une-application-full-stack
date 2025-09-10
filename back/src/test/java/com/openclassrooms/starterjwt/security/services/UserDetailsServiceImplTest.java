package com.openclassrooms.starterjwt.security.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	UserRepository userRepository;

	@InjectMocks
	UserDetailsServiceImpl service;

	private User newUser(Long id, String email, String first, String last, String pwd) {
		User u = new User();
		u.setId(id);
		u.setEmail(email);
		u.setFirstName(first);
		u.setLastName(last);
		u.setPassword(pwd);
		return u;
	}

	@Test
	void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
		// GIVEN
		String email = "alice@example.com";
		User user = newUser(1L, email, "Alice", "Wonder", "hashedPwd");
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		// WHEN
		UserDetails details = service.loadUserByUsername(email);

		// THEN
		assertThat(details).isInstanceOf(UserDetailsImpl.class);
		assertThat(details.getUsername()).isEqualTo(email);
		assertThat(details.getPassword()).isEqualTo("hashedPwd");

		UserDetailsImpl impl = (UserDetailsImpl) details;
		assertThat(impl.getId()).isEqualTo(1L);
		assertThat(impl.getFirstName()).isEqualTo("Alice");
		assertThat(impl.getLastName()).isEqualTo("Wonder");

		verify(userRepository).findByEmail(email);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	void loadUserByUsername_shouldThrow_whenUserNotFound() {
		// GIVEN
		String email = "missing@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		// WHEN
		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(email));

		// THEN
		verify(userRepository).findByEmail(email);
		verifyNoMoreInteractions(userRepository);
	}
}
