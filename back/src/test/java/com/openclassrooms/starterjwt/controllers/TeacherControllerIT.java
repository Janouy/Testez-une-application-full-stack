package com.openclassrooms.starterjwt.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;

@SpringBootTest(classes = { TeacherControllerIT.TestApp.class,
		TeacherControllerIT.MapStructConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TeacherControllerIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "com.openclassrooms.starterjwt")
	static class TestApp {
	}

	@Configuration
	static class MapStructConfig {
		@Bean
		TeacherMapper teacherMapper() {
			return Mappers.getMapper(TeacherMapper.class);
		}
	}

	@Autowired
	private MockMvc mvc;

	@MockBean
	private TeacherService teacherService;

	private Teacher teacher(long id, String firstName, String lastName) {
		return new Teacher().setId(id).setFirstName(firstName).setLastName(lastName)
				.setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void findById_ok_returns200_andBody() throws Exception {
		when(teacherService.findById(1L)).thenReturn(teacher(1L, "Alice", "Doe"));

		mvc.perform(get("/api/teacher/{id}", "1").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				// Champs du TeacherDto
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.firstName").value("Alice"))
				.andExpect(jsonPath("$.lastName").value("Doe")).andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.updatedAt").exists());
	}

	@Test
	@WithMockUser
	void findById_notFound_returns404() throws Exception {
		when(teacherService.findById(42L)).thenReturn(null);

		mvc.perform(get("/api/teacher/{id}", "42")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void findById_badRequest_whenIdNotNumeric_returns400() throws Exception {
		mvc.perform(get("/api/teacher/{id}", "abc")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "USER")
	void findAll_ok_returns200_andList() throws Exception {
		when(teacherService.findAll()).thenReturn(List.of(teacher(1L, "Alice", "Doe"), teacher(2L, "Bob", "Smith")));

		mvc.perform(get("/api/teacher").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].firstName").value("Alice")).andExpect(jsonPath("$[0].lastName").value("Doe"))
				.andExpect(jsonPath("$[1].id").value(2)).andExpect(jsonPath("$[1].firstName").value("Bob"))
				.andExpect(jsonPath("$[1].lastName").value("Smith"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void findAll_ok_whenEmpty_returns200_andEmptyArray() throws Exception {
		when(teacherService.findAll()).thenReturn(List.of());

		mvc.perform(get("/api/teacher").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(0)));
	}
}
