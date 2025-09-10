package com.openclassrooms.starterjwt.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

	@Mock
	private TeacherRepository teacherRepository;

	@InjectMocks
	TeacherService service;

	private Teacher newTeacher(Long id) {
		Teacher teacher = new Teacher();
		teacher.setId(id);
		return teacher;
	}

	@Test
	void findAll_shouldReturnListFromRepository() {
		List<Teacher> list = List.of(newTeacher(1L), newTeacher(2L));
		when(teacherRepository.findAll()).thenReturn(list);

		List<Teacher> result = service.findAll();

		assertThat(result).hasSize(2);
		verify(teacherRepository).findAll();
		verifyNoMoreInteractions(teacherRepository);
	}

	@Test
	void findById_shouldReturnTeacher_whenFound() {
		// GIVEN
		Long id = 1L;
		Teacher teacher = new Teacher();
		teacher.setId(id);
		when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));

		// WHEN
		Teacher result = service.findById(id);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(id);
		verify(teacherRepository).findById(id);
		verifyNoMoreInteractions(teacherRepository);
	}

	@Test
	void findById_shouldReturnNull_whenNotFound() {
		// GIVEN
		Long id = 999L;
		when(teacherRepository.findById(id)).thenReturn(Optional.empty());

		// WHEN
		Teacher result = service.findById(id);

		// THEN
		assertThat(result).isNull();
		verify(teacherRepository).findById(id);
		verifyNoMoreInteractions(teacherRepository);
	}

}
