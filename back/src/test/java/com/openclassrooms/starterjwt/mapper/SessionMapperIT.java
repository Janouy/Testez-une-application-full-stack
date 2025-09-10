package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;

@SpringBootTest(classes = SessionMapperIT.TestApp.class)
class SessionMapperIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "com.openclassrooms.starterjwt")
	static class TestApp {
	}

	@Autowired
	private SessionMapper mapper;

	@MockBean
	private TeacherService teacherService;
	@MockBean
	private UserService userService;

	@Test
	void springBean_wiring_and_mapping_roundtrip() {
		assertThat(mapper).isNotNull();

		SessionDto dto = new SessionDto();
		dto.setName("Morning");
		dto.setDate(new Date(1704067200000L));
		dto.setTeacher_id(33L);
		dto.setDescription("Desc");
		dto.setUsers(Arrays.asList(1L, 2L));

		Teacher t = new Teacher();
		t.setId(33L);
		User u1 = new User().setId(1L).setEmail("u1@ex.com").setFirstName("U1").setLastName("A").setPassword("x")
				.setAdmin(false);
		User u2 = new User().setId(2L).setEmail("u2@ex.com").setFirstName("U2").setLastName("B").setPassword("x")
				.setAdmin(false);
		when(teacherService.findById(33L)).thenReturn(t);
		when(userService.findById(1L)).thenReturn(u1);
		when(userService.findById(2L)).thenReturn(u2);

		Session s = mapper.toEntity(dto);
		assertThat(s.getTeacher().getId()).isEqualTo(33L);
		assertThat(s.getUsers()).containsExactly(u1, u2);

		verify(teacherService).findById(33L);
		verify(userService).findById(1L);
		verify(userService).findById(2L);

		SessionDto back = mapper.toDto(s);
		assertThat(back.getTeacher_id()).isEqualTo(33L);
		assertThat(back.getUsers()).containsExactlyInAnyOrder(1L, 2L);
	}
}
