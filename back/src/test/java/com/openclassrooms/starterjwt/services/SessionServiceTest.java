package com.openclassrooms.starterjwt.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

	@Mock
	private SessionRepository sessionRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	SessionService service;

	private Session newSession(Long id) {
		Session session = new Session();
		session.setId(id);
		session.setUsers(new ArrayList<>());
		return session;
	}

	private User newUser(Long id) {
		User user = new User();
		user.setId(id);
		return user;
	}

	@Test
	void create_shouldSaveAndReturn() {
		// GIVEN
		Session toSave = newSession(null);
		Session saved = newSession(10L);

		// WHEN
		when(sessionRepository.save(toSave)).thenReturn(saved);

		Session result = service.create(toSave);
		// THEN
		assertThat(result.getId()).isEqualTo(10L);
		verify(sessionRepository).save(toSave);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void delete_shouldDelegateToRepository() {
		// GIVEN
		Long id = 42L;

		// WHEN
		service.delete(id);

		// THEN
		verify(sessionRepository).deleteById(id);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void findAll_shouldReturnListFromRepository() {
		List<Session> list = List.of(newSession(1L), newSession(2L));
		when(sessionRepository.findAll()).thenReturn(list);

		List<Session> result = service.findAll();

		assertThat(result).hasSize(2);
		verify(sessionRepository).findAll();
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void getById_shouldReturnSession_whenFound() {
		when(sessionRepository.findById(7L)).thenReturn(Optional.of(newSession(7L)));

		Session result = service.getById(7L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(7L);
		verify(sessionRepository).findById(7L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void getById_shouldReturnNull_whenNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		Session result = service.getById(99L);

		assertThat(result).isNull();
		verify(sessionRepository).findById(99L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void update_shouldSetIdAndSave() {
		Session payload = newSession(null);
		when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

		Session result = service.update(123L, payload);

		assertThat(result.getId()).isEqualTo(123L);
		ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
		verify(sessionRepository).save(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(123L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void participate_shouldAddUser_whenSessionAndUserExist_andNotAlreadyParticipating() {
		Long sessionId = 1L, userId = 2L;
		Session session = newSession(sessionId);
		User user = newUser(userId);

		when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		service.participate(sessionId, userId);

		assertThat(session.getUsers()).extracting(User::getId).containsExactly(userId);
		verify(sessionRepository).findById(sessionId);
		verify(userRepository).findById(userId);
		verify(sessionRepository).save(session);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void participate_shouldThrowNotFound_whenSessionMissing() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
		when(userRepository.findById(2L)).thenReturn(Optional.of(newUser(2L)));

		assertThatThrownBy(() -> service.participate(1L, 2L)).isInstanceOf(NotFoundException.class);

		verify(sessionRepository).findById(1L);
		verify(userRepository).findById(2L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void participate_shouldThrowNotFound_whenUserMissing() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(newSession(1L)));
		when(userRepository.findById(2L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.participate(1L, 2L)).isInstanceOf(NotFoundException.class);

		verify(sessionRepository).findById(1L);
		verify(userRepository).findById(2L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void noLongerParticipate_shouldRemoveUser_whenParticipating() {
		Long sessionId = 1L, userId = 2L;
		Session session = newSession(sessionId);
		session.getUsers().add(newUser(userId));

		when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

		service.noLongerParticipate(sessionId, userId);

		assertThat(session.getUsers()).isEmpty();
		verify(sessionRepository).findById(sessionId);
		verify(sessionRepository).save(session);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void noLongerParticipate_shouldThrowNotFound_whenSessionMissing() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.noLongerParticipate(1L, 2L)).isInstanceOf(NotFoundException.class);

		verify(sessionRepository).findById(1L);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}

	@Test
	void noLongerParticipate_shouldThrowBadRequest_whenUserNotParticipating() {
		Long sessionId = 1L, userId = 2L;
		Session session = newSession(sessionId);

		when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> service.noLongerParticipate(sessionId, userId))
				.isInstanceOf(BadRequestException.class);

		verify(sessionRepository).findById(sessionId);
		verifyNoMoreInteractions(sessionRepository, userRepository);
	}
}
