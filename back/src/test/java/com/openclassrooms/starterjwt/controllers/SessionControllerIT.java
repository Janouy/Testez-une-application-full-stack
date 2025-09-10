package com.openclassrooms.starterjwt.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;

@SpringBootTest(classes = SessionControllerIT.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SessionControllerIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "com.openclassrooms.starterjwt")
	static class TestApp {
	}

	@Autowired
	private MockMvc mvc;

	@MockBean
	private SessionService sessionService;
	@MockBean
	private SessionMapper sessionMapper;

	private Session entity(long id, String name) {
		return new Session().setId(id).setName(name).setDate(new Date(1704067200000L)) // 2024-01-01T00:00:00Z approx
				.setDescription("Morning flow").setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));
	}

	private SessionDto dto(long id, String name, long teacherId) {
		SessionDto d = new SessionDto();
		d.setId(id);
		d.setName(name);
		d.setDate(new Date(1704067200000L));
		d.setTeacher_id(teacherId);
		d.setDescription("Morning flow");
		d.setUsers(List.of(1L, 2L));
		d.setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"));
		d.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));
		return d;
	}

	@Test
	@WithMockUser
	void findById_ok_returns200_andBody() throws Exception {
		Session s = entity(1L, "Morning Yoga");
		SessionDto d = dto(1L, "Morning Yoga", 99L);

		when(sessionService.getById(1L)).thenReturn(s);
		when(sessionMapper.toDto(s)).thenReturn(d);

		mvc.perform(get("/api/session/{id}", "1")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json")).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Morning Yoga")).andExpect(jsonPath("$.teacher_id").value(99))
				.andExpect(jsonPath("$.description").value("Morning flow")).andExpect(jsonPath("$.users", hasSize(2)))
				.andExpect(jsonPath("$.createdAt").exists()).andExpect(jsonPath("$.updatedAt").exists());
	}

	@Test
	@WithMockUser
	void findById_notFound_returns404() throws Exception {
		when(sessionService.getById(42L)).thenReturn(null);

		mvc.perform(get("/api/session/{id}", "42")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void findById_badRequest_whenIdNotNumeric_returns400() throws Exception {
		mvc.perform(get("/api/session/{id}", "abc")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void findAll_ok_returns200_andList() throws Exception {
		Session s1 = entity(1L, "Morning Yoga");
		Session s2 = entity(2L, "Evening Flow");
		SessionDto d1 = dto(1L, "Morning Yoga", 99L);
		SessionDto d2 = dto(2L, "Evening Flow", 88L);

		when(sessionService.findAll()).thenReturn(List.of(s1, s2));
		when(sessionMapper.toDto(List.of(s1, s2))).thenReturn(List.of(d1, d2));

		mvc.perform(get("/api/session")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json")).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[0].name").value("Morning Yoga"))
				.andExpect(jsonPath("$[1].id").value(2)).andExpect(jsonPath("$[1].name").value("Evening Flow"));
	}

	@Test
	@WithMockUser
	void findAll_ok_whenEmpty_returns200_andEmptyArray() throws Exception {
		when(sessionService.findAll()).thenReturn(List.of());
		when(sessionMapper.toDto(List.of())).thenReturn(List.of());

		mvc.perform(get("/api/session")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@WithMockUser
	void create_ok_returns200_andBody() throws Exception {
		String body = "{\n" + "  \"name\": \"Morning Yoga\",\n" + "  \"date\": 1704067200000,\n" +

				"  \"teacher_id\": 99,\n" + "  \"description\": \"Morning flow\",\n" + "  \"users\": [1,2]\n" + "}";

		SessionDto inDto = dto(80L, "Morning Yoga", 99L);
		Session inEntity = entity(0L, "Morning Yoga");
		Session savedEntity = entity(1L, "Morning Yoga");
		SessionDto outDto = dto(1L, "Morning Yoga", 99L);

		when(sessionMapper.toEntity(Mockito.any(SessionDto.class))).thenReturn(inEntity);
		when(sessionService.create(inEntity)).thenReturn(savedEntity);
		when(sessionMapper.toDto(savedEntity)).thenReturn(outDto);

		mvc.perform(post("/api/session").with(csrf()).contentType("application/json").content(body)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Morning Yoga"))
				.andExpect(jsonPath("$.teacher_id").value(99));

		ArgumentCaptor<Session> cap = ArgumentCaptor.forClass(Session.class);
		verify(sessionService).create(cap.capture());
	}

	@Test
	@WithMockUser
	void update_ok_returns200_andBody() throws Exception {
		String body = "{\n" + "  \"id\": 1,\n" + "  \"name\": \"Morning Yoga UPDATED\",\n"
				+ "  \"date\": 1704067200000,\n" + "  \"teacher_id\": 77,\n" + "  \"description\": \"Updated\",\n"
				+ "  \"users\": [1]\n" + "}";

		SessionDto inDto = dto(1L, "Morning Yoga UPDATED", 77L);
		Session inEntity = entity(1L, "Morning Yoga UPDATED");
		Session updated = entity(1L, "Morning Yoga UPDATED");
		SessionDto outDto = dto(1L, "Morning Yoga UPDATED", 77L);

		when(sessionMapper.toEntity(Mockito.any(SessionDto.class))).thenReturn(inEntity);
		when(sessionService.update(1L, inEntity)).thenReturn(updated);
		when(sessionMapper.toDto(updated)).thenReturn(outDto);

		mvc.perform(put("/api/session/{id}", "1").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json")).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Morning Yoga UPDATED"))
				.andExpect(jsonPath("$.teacher_id").value(77));
	}

	@Test
	@WithMockUser
	void update_badRequest_whenIdNotNumeric_returns400() throws Exception {
		String body = "{ \"name\": \"X\", \"date\": 1704067200000, \"teacher_id\": 1, \"description\": \"d\" }";

		mvc.perform(put("/api/session/{id}", "abc").with(csrf()).contentType("application/json").content(body))
				.andDo(print()).andExpect(status().isBadRequest());

		verify(sessionService, never()).update(anyLong(), any(Session.class));
	}

	@Test
	@WithMockUser
	void delete_ok_whenExists_returns200_andCallsService() throws Exception {
		when(sessionService.getById(1L)).thenReturn(entity(1L, "S"));

		mvc.perform(delete("/api/session/{id}", "1").with(csrf())).andDo(print()).andExpect(status().isOk());

		verify(sessionService).delete(1L);
	}

	@Test
	@WithMockUser
	void delete_notFound_whenMissing_returns404_andNoDelete() throws Exception {
		when(sessionService.getById(9L)).thenReturn(null);

		mvc.perform(delete("/api/session/{id}", "9").with(csrf())).andDo(print()).andExpect(status().isNotFound());

		verify(sessionService, never()).delete(anyLong());
	}

	@Test
	@WithMockUser
	void delete_badRequest_whenIdNotNumeric_returns400() throws Exception {
		mvc.perform(delete("/api/session/{id}", "xyz").with(csrf())).andDo(print()).andExpect(status().isBadRequest());

		verify(sessionService, never()).delete(anyLong());
	}

	@Test
	@WithMockUser
	void participate_ok_returns200_andCallsService() throws Exception {
		mvc.perform(post("/api/session/{id}/participate/{userId}", "5", "12").with(csrf())).andDo(print())
				.andExpect(status().isOk());

		verify(sessionService).participate(5L, 12L);
	}

	@Test
	@WithMockUser
	void participate_badRequest_whenNonNumeric_returns400_andNoCall() throws Exception {
		mvc.perform(post("/api/session/{id}/participate/{userId}", "abc", "12").with(csrf())).andDo(print())
				.andExpect(status().isBadRequest());

		verify(sessionService, never()).participate(anyLong(), anyLong());
	}

	@Test
	@WithMockUser
	void noLongerParticipate_ok_returns200_andCallsService() throws Exception {
		mvc.perform(delete("/api/session/{id}/participate/{userId}", "5", "12").with(csrf())).andDo(print())
				.andExpect(status().isOk());

		verify(sessionService).noLongerParticipate(5L, 12L);
	}

	@Test
	@WithMockUser
	void noLongerParticipate_badRequest_whenNonNumeric_returns400_andNoCall() throws Exception {
		mvc.perform(delete("/api/session/{id}/participate/{userId}", "5", "abc").with(csrf())).andDo(print())
				.andExpect(status().isBadRequest());

		verify(sessionService, never()).noLongerParticipate(anyLong(), anyLong());
	}
}
