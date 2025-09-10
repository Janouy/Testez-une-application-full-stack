package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;

@SpringJUnitConfig
@Import(TeacherMapperImpl.class)
class TeacherMapperIT {

	@Autowired
	private TeacherMapper mapper;

	@Test
	void wiring_and_roundtrip_mapping() {
		assertThat(mapper).isNotNull();

		Teacher entity = new Teacher();
		entity.setId(10L);
		entity.setLastName("Durand");
		entity.setFirstName("Alice");
		entity.setCreatedAt(LocalDateTime.parse("2024-03-01T00:00:00"));
		entity.setUpdatedAt(LocalDateTime.parse("2024-03-02T00:00:00"));

		TeacherDto dto = mapper.toDto(entity);
		assertThat(dto.getId()).isEqualTo(10L);
		assertThat(dto.getLastName()).isEqualTo("Durand");
		assertThat(dto.getFirstName()).isEqualTo("Alice");
		assertThat(dto.getCreatedAt()).isNotNull();
		assertThat(dto.getUpdatedAt()).isNotNull();

		Teacher back = mapper.toEntity(dto);
		assertThat(back.getId()).isEqualTo(10L);
		assertThat(back.getLastName()).isEqualTo("Durand");
		assertThat(back.getFirstName()).isEqualTo("Alice");
		assertThat(back.getCreatedAt()).isNotNull();
		assertThat(back.getUpdatedAt()).isNotNull();
	}
}
