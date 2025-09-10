package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@Mock
	private SessionMapper sessionMapper;

	private SessionController controller;

	@BeforeEach
	void setUp() {
		controller = new SessionController(sessionService, sessionMapper);
	}

	@Test
	void findById_whenValidIdAndFound_returns200WithDto() {
		Session entity = new Session();
		SessionDto dto = new SessionDto();

		when(sessionService.getById(10L)).thenReturn(entity);
		when(sessionMapper.toDto(entity)).thenReturn(dto);

		ResponseEntity<?> resp = controller.findById("10");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(dto);
	}

	@Test
	void findById_whenValidIdAndNotFound_returns404() {
		when(sessionService.getById(99L)).thenReturn(null);

		ResponseEntity<?> resp = controller.findById("99");

		assertThat(resp.getStatusCodeValue()).isEqualTo(404);
		assertThat(resp.getBody()).isNull();
	}

	@Test
	void findById_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.findById("abc");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
	}

	@Test
	void findAll_returns200WithMappedList() {
		Session s1 = new Session();
		Session s2 = new Session();
		List<Session> entities = Arrays.asList(s1, s2);
		List<SessionDto> dtos = Arrays.asList(new SessionDto(), new SessionDto());

		when(sessionService.findAll()).thenReturn(entities);
		when(sessionMapper.toDto(entities)).thenReturn(dtos);

		ResponseEntity<?> resp = controller.findAll();

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(dtos);
	}

	@Test
	void findAll_handlesEmptyList() {
		when(sessionService.findAll()).thenReturn(Collections.emptyList());
		when(sessionMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());

		ResponseEntity<?> resp = controller.findAll();

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isInstanceOf(List.class);
		assertThat((List<?>) resp.getBody()).isEmpty();
	}

	@Test
	void create_returns200WithCreatedDto_andCallsServiceWithMappedEntity() {
		SessionDto inputDto = new SessionDto();
		Session mappedEntity = new Session();
		Session createdEntity = new Session();
		SessionDto outputDto = new SessionDto();

		when(sessionMapper.toEntity(inputDto)).thenReturn(mappedEntity);
		when(sessionService.create(mappedEntity)).thenReturn(createdEntity);
		when(sessionMapper.toDto(createdEntity)).thenReturn(outputDto);

		ResponseEntity<?> resp = controller.create(inputDto);

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(outputDto);
		verify(sessionMapper).toEntity(inputDto);
		verify(sessionService).create(mappedEntity);
		verify(sessionMapper).toDto(createdEntity);
	}

	@Test
	void update_whenValidId_returns200WithDto() {
		SessionDto inputDto = new SessionDto();
		Session toUpdateEntity = new Session();
		Session updatedEntity = new Session();
		SessionDto outputDto = new SessionDto();

		when(sessionMapper.toEntity(inputDto)).thenReturn(toUpdateEntity);
		when(sessionService.update(5L, toUpdateEntity)).thenReturn(updatedEntity);
		when(sessionMapper.toDto(updatedEntity)).thenReturn(outputDto);

		ResponseEntity<?> resp = controller.update("5", inputDto);

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(outputDto);
	}

	@Test
	void update_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.update("NaN", new SessionDto());
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verifyNoInteractions(sessionService);
	}

	@Test
	void delete_whenValidIdAndFound_returns200_andCallsDelete() {
		Session existing = new Session();
		when(sessionService.getById(3L)).thenReturn(existing);

		ResponseEntity<?> resp = controller.save("3");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		verify(sessionService).delete(3L);
	}

	@Test
	void delete_whenValidIdButNotFound_returns404() {
		when(sessionService.getById(404L)).thenReturn(null);

		ResponseEntity<?> resp = controller.save("404");

		assertThat(resp.getStatusCodeValue()).isEqualTo(404);
		verify(sessionService, never()).delete(any());
	}

	@Test
	void delete_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.save("oops");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verify(sessionService, never()).delete(any());
	}

	@Test
	void participate_whenValidIds_returns200_andCallsService() {
		ResponseEntity<?> resp = controller.participate("11", "22");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		verify(sessionService).participate(11L, 22L);
	}

	@Test
	void participate_whenBadSessionId_returns400() {
		ResponseEntity<?> resp = controller.participate("bad", "22");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verify(sessionService, never()).participate(any(), any());
	}

	@Test
	void participate_whenBadUserId_returns400() {
		ResponseEntity<?> resp = controller.participate("11", "bad");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verify(sessionService, never()).participate(any(), any());
	}

	@Test
	void noLongerParticipate_whenValidIds_returns200_andCallsService() {
		ResponseEntity<?> resp = controller.noLongerParticipate("7", "8");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		verify(sessionService).noLongerParticipate(7L, 8L);
	}

	@Test
	void noLongerParticipate_whenBadIds_returns400() {
		ResponseEntity<?> resp1 = controller.noLongerParticipate("bad", "8");
		ResponseEntity<?> resp2 = controller.noLongerParticipate("7", "bad");

		assertThat(resp1.getStatusCodeValue()).isEqualTo(400);
		assertThat(resp2.getStatusCodeValue()).isEqualTo(400);
		verify(sessionService, never()).noLongerParticipate(any(), any());
	}
}
