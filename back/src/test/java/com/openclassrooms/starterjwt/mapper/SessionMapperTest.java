package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;

@ExtendWith(MockitoExtension.class)
class SessionMapperTest {

	private SessionMapper mapper;

	@Mock
	TeacherService teacherService;
	@Mock
	UserService userService;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(SessionMapper.class);
		ReflectionTestUtils.setField(mapper, "teacherService", teacherService);
		ReflectionTestUtils.setField(mapper, "userService", userService);
	}

	@Test
	void toEntity_mapsFields_callsServices_and_keepsNullsForMissingUsers() {
		// given
		SessionDto dto = new SessionDto();
		dto.setId(null);
		dto.setName("Morning Yoga");
		dto.setDate(new Date(1704067200000L));
		dto.setTeacher_id(10L);
		dto.setDescription("Desc");
		dto.setUsers(Arrays.asList(1L, 2L, 99L));
		dto.setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"));
		dto.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));

		Teacher teacher = new Teacher();
		teacher.setId(10L);

		User u1 = new User().setId(1L).setEmail("u1@ex.com").setFirstName("U1").setLastName("A").setPassword("x")
				.setAdmin(false);
		User u2 = new User().setId(2L).setEmail("u2@ex.com").setFirstName("U2").setLastName("B").setPassword("x")
				.setAdmin(false);

		when(teacherService.findById(10L)).thenReturn(teacher);
		when(userService.findById(1L)).thenReturn(u1);
		when(userService.findById(2L)).thenReturn(u2);
		when(userService.findById(99L)).thenReturn(null);

		// when
		Session entity = mapper.toEntity(dto);

		// then
		assertThat(entity.getName()).isEqualTo("Morning Yoga");
		assertThat(entity.getDate()).isEqualTo(new Date(1704067200000L));
		assertThat(entity.getDescription()).isEqualTo("Desc");
		assertThat(entity.getTeacher()).isNotNull();
		assertThat(entity.getTeacher().getId()).isEqualTo(10L);

		assertThat(entity.getUsers()).hasSize(3);
		assertThat(entity.getUsers()).contains(u1, u2);
		assertThat(entity.getUsers()).contains((User) null);

		verify(teacherService).findById(10L);
		verify(userService).findById(1L);
		verify(userService).findById(2L);
		verify(userService).findById(99L);
	}

	@Test
	void toEntity_handlesNullTeacherId_andNullUsers_asEmptyList() {
		// given
		SessionDto dto = new SessionDto();
		dto.setName("No teacher/users");
		dto.setDate(new Date());
		dto.setTeacher_id(null);
		dto.setDescription("x");
		dto.setUsers(null);

		// when
		Session entity = mapper.toEntity(dto);

		// then
		assertThat(entity.getTeacher()).isNull();
		assertThat(entity.getUsers()).isNotNull();
		assertThat(entity.getUsers()).isEmpty();

		verifyNoInteractions(teacherService, userService);
	}

	@Test
	void toDto_mapsFields_and_flattensUsersIds() {
		// given
		Session s = new Session();
		s.setId(5L);
		s.setName("Evening");
		s.setDate(new Date(1704153600000L));
		s.setDescription("ZZ");

		Teacher teacher = new Teacher();
		teacher.setId(20L);
		s.setTeacher(teacher);

		User u1 = new User().setId(1L).setEmail("a@ex.com").setFirstName("A").setLastName("A").setPassword("x")
				.setAdmin(false);
		User u7 = new User().setId(7L).setEmail("b@ex.com").setFirstName("B").setLastName("B").setPassword("x")
				.setAdmin(false);
		s.setUsers(Arrays.asList(u1, u7));

		s.setCreatedAt(LocalDateTime.parse("2024-01-03T00:00:00"));
		s.setUpdatedAt(LocalDateTime.parse("2024-01-04T00:00:00"));

		// when
		SessionDto dto = mapper.toDto(s);

		// then
		assertThat(dto.getId()).isEqualTo(5L);
		assertThat(dto.getName()).isEqualTo("Evening");
		assertThat(dto.getTeacher_id()).isEqualTo(20L);
		assertThat(dto.getDescription()).isEqualTo("ZZ");
		assertThat(dto.getUsers()).containsExactlyInAnyOrder(1L, 7L);
		assertThat(dto.getCreatedAt()).isNotNull();
		assertThat(dto.getUpdatedAt()).isNotNull();
	}

	@Test
	void toDto_handlesNullUsers_asEmptyList() {
		// given
		Session s = new Session();
		s.setUsers(null);

		// when
		SessionDto dto = mapper.toDto(s);

		// then
		assertThat(dto.getUsers()).isNotNull();
		assertThat(dto.getUsers()).isEmpty();
	}
}
