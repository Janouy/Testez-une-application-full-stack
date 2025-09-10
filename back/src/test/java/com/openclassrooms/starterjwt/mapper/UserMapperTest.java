package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;

class UserMapperTest {

	private UserMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(UserMapper.class);
		assertThat(mapper).isNotNull();
	}

	private User user(long id, String email, String first, String last, boolean admin, String password) {
		return new User().setId(id).setEmail(email).setFirstName(first).setLastName(last).setPassword(password)
				.setAdmin(admin).setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));
	}

	@Test
	void toDto_maps_all_fields() {
		User entity = user(1L, "alice@example.com", "Alice", "Doe", true, "hash");
		UserDto dto = mapper.toDto(entity);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getEmail()).isEqualTo("alice@example.com");
		assertThat(dto.getFirstName()).isEqualTo("Alice");
		assertThat(dto.getLastName()).isEqualTo("Doe");
		assertThat(dto.isAdmin()).isTrue();
		assertThat(dto.getPassword()).isEqualTo("hash");
		assertThat(dto.getCreatedAt()).isNotNull();
		assertThat(dto.getUpdatedAt()).isNotNull();
	}

	@Test
	void toEntity_maps_all_fields() {
		UserDto dto = new UserDto(2L, "bob@example.com", "Doe", "Bob", false, "pwd",
				LocalDateTime.parse("2024-02-01T00:00:00"), LocalDateTime.parse("2024-02-02T00:00:00"));

		User entity = mapper.toEntity(dto);

		assertThat(entity.getId()).isEqualTo(2L);
		assertThat(entity.getEmail()).isEqualTo("bob@example.com");
		assertThat(entity.getFirstName()).isEqualTo("Bob");
		assertThat(entity.getLastName()).isEqualTo("Doe");
		assertThat(entity.isAdmin()).isFalse();
		assertThat(entity.getPassword()).isEqualTo("pwd");
		assertThat(entity.getCreatedAt()).isNotNull();
		assertThat(entity.getUpdatedAt()).isNotNull();
	}

	@Test
	void list_mappings_work_both_ways() {
		List<User> entities = List.of(user(1L, "a@ex.com", "A", "One", false, "x"),
				user(2L, "b@ex.com", "B", "Two", true, "y"));

		List<UserDto> dtos = mapper.toDto(entities);
		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getEmail()).isEqualTo("a@ex.com");
		assertThat(dtos.get(1).isAdmin()).isTrue();

		List<User> back = mapper.toEntity(dtos);
		assertThat(back).hasSize(2);
		assertThat(back.get(0).getEmail()).isEqualTo("a@ex.com");
		assertThat(back.get(1).isAdmin()).isTrue();
	}

	@Test
	void null_inputs_return_null() {
		assertThat(mapper.toDto((User) null)).isNull();
		assertThat(mapper.toEntity((UserDto) null)).isNull();

		assertThat(mapper.toDto((java.util.List<User>) null)).isNull();
		assertThat(mapper.toEntity((java.util.List<UserDto>) null)).isNull();
	}

}
