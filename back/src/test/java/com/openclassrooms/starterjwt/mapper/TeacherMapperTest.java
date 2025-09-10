package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;

class TeacherMapperTest {

	private TeacherMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(TeacherMapper.class);
		assertThat(mapper).isNotNull();
	}

	@Test
	void toDto_maps_all_fields() {
		Teacher entity = new Teacher();
		entity.setId(1L);
		entity.setLastName("Doe");
		entity.setFirstName("Jane");
		entity.setCreatedAt(LocalDateTime.parse("2024-01-01T10:00:00"));
		entity.setUpdatedAt(LocalDateTime.parse("2024-01-02T10:00:00"));

		TeacherDto dto = mapper.toDto(entity);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getLastName()).isEqualTo("Doe");
		assertThat(dto.getFirstName()).isEqualTo("Jane");
		assertThat(dto.getCreatedAt()).isNotNull();
		assertThat(dto.getUpdatedAt()).isNotNull();
	}

	@Test
	void toEntity_maps_all_fields() {
		TeacherDto dto = new TeacherDto();
		dto.setId(2L);
		dto.setLastName("Smith");
		dto.setFirstName("John");
		dto.setCreatedAt(LocalDateTime.parse("2024-02-01T08:00:00"));
		dto.setUpdatedAt(LocalDateTime.parse("2024-02-02T08:00:00"));

		Teacher entity = mapper.toEntity(dto);

		assertThat(entity.getId()).isEqualTo(2L);
		assertThat(entity.getLastName()).isEqualTo("Smith");
		assertThat(entity.getFirstName()).isEqualTo("John");
		assertThat(entity.getCreatedAt()).isNotNull();
		assertThat(entity.getUpdatedAt()).isNotNull();
	}

	@Test
	void null_inputs_return_null() {
		assertThat(mapper.toDto((Teacher) null)).isNull();
		assertThat(mapper.toEntity((TeacherDto) null)).isNull();
	}

	@Test
	void toEntity_list_null_returns_null() {
		assertThat(mapper.toEntity((List<TeacherDto>) null)).isNull();
	}

	@Test
	void toEntity_list_empty_returns_empty() {
		assertThat(mapper.toEntity(Collections.emptyList())).isEmpty();
	}

	@Test
	void toEntity_list_maps_items_and_preserves_null_elements() {
		TeacherDto d1 = new TeacherDto(1L, "Doe", "Jane", LocalDateTime.parse("2024-01-01T10:00:00"),
				LocalDateTime.parse("2024-01-02T10:00:00"));
		TeacherDto d3 = new TeacherDto(3L, "Lovelace", "Ada", null, null);

		List<Teacher> out = mapper.toEntity(Arrays.asList(d1, null, d3));

		assertThat(out).hasSize(3);
		assertThat(out.get(0).getId()).isEqualTo(1L);
		assertThat(out.get(1)).isNull();
		assertThat(out.get(2).getFirstName()).isEqualTo("Ada");
	}

	@Test
	void toDto_list_null_returns_null() {
		assertThat(mapper.toDto((List<Teacher>) null)).isNull();
	}

	@Test
	void toDto_list_empty_returns_empty() {
		assertThat(mapper.toDto(Collections.emptyList())).isEmpty();
	}

	@Test
	void toDto_list_maps_items_and_preserves_null_elements() {
		Teacher t1 = new Teacher();
		t1.setId(10L);
		t1.setLastName("Durand");
		t1.setFirstName("Alice");

		Teacher t3 = new Teacher();
		t3.setId(30L);
		t3.setLastName("Turing");
		t3.setFirstName("Alan");

		List<TeacherDto> out = mapper.toDto(Arrays.asList(t1, null, t3));

		assertThat(out).hasSize(3);
		assertThat(out.get(0).getId()).isEqualTo(10L);
		assertThat(out.get(1)).isNull();
		assertThat(out.get(2).getFirstName()).isEqualTo("Alan");
	}

}
