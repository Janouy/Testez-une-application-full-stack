package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

	@Mock
	private TeacherService teacherService;

	@Mock
	private TeacherMapper teacherMapper;

	private TeacherController controller;

	@BeforeEach
	void setUp() {
		controller = new TeacherController(teacherService, teacherMapper);
	}

	@Test
	void findById_whenValidIdAndFound_returns200WithDto() {
		Teacher entity = new Teacher();
		TeacherDto dto = new TeacherDto();

		when(teacherService.findById(5L)).thenReturn(entity);
		when(teacherMapper.toDto(entity)).thenReturn(dto);

		ResponseEntity<?> resp = controller.findById("5");

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(dto);
	}

	@Test
	void findById_whenValidIdAndNotFound_returns404() {
		when(teacherService.findById(404L)).thenReturn(null);

		ResponseEntity<?> resp = controller.findById("404");

		assertThat(resp.getStatusCodeValue()).isEqualTo(404);
		assertThat(resp.getBody()).isNull();
	}

	@Test
	void findById_whenBadId_returns400() {
		ResponseEntity<?> resp = controller.findById("abc");
		assertThat(resp.getStatusCodeValue()).isEqualTo(400);
		verifyNoInteractions(teacherMapper);
	}

	@Test
	void findAll_returns200WithMappedList() {
		Teacher t1 = new Teacher();
		Teacher t2 = new Teacher();
		List<Teacher> entities = Arrays.asList(t1, t2);
		List<TeacherDto> dtos = Arrays.asList(new TeacherDto(), new TeacherDto());

		when(teacherService.findAll()).thenReturn(entities);
		when(teacherMapper.toDto(entities)).thenReturn(dtos);

		ResponseEntity<?> resp = controller.findAll();

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isSameAs(dtos);
	}

	@Test
	void findAll_handlesEmptyList() {
		when(teacherService.findAll()).thenReturn(Collections.emptyList());
		when(teacherMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());

		ResponseEntity<?> resp = controller.findAll();

		assertThat(resp.getStatusCodeValue()).isEqualTo(200);
		assertThat(resp.getBody()).isInstanceOf(List.class);
		assertThat((List<?>) resp.getBody()).isEmpty();
	}
}
