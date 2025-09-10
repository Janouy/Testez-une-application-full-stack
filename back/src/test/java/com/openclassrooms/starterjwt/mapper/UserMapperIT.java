
package com.openclassrooms.starterjwt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;

@SpringJUnitConfig
@Import(UserMapperImpl.class)
class UserMapperIT {

	@Autowired
	private UserMapper mapper;

	@Test
	void wiring_and_roundtrip_mapping() {
		assertThat(mapper).isNotNull();

		User entity = new User().setId(10L).setEmail("wired@example.com").setFirstName("Wired").setLastName("Bean")
				.setPassword("p").setAdmin(false).setCreatedAt(LocalDateTime.parse("2024-03-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-03-02T00:00:00"));

		UserDto dto = mapper.toDto(entity);
		assertThat(dto.getId()).isEqualTo(10L);
		assertThat(dto.getEmail()).isEqualTo("wired@example.com");
		assertThat(dto.getFirstName()).isEqualTo("Wired");
		assertThat(dto.getLastName()).isEqualTo("Bean");
		assertThat(dto.isAdmin()).isFalse();
		assertThat(dto.getCreatedAt()).isNotNull();
		assertThat(dto.getUpdatedAt()).isNotNull();

		User back = mapper.toEntity(dto);
		assertThat(back.getId()).isEqualTo(10L);
		assertThat(back.getEmail()).isEqualTo("wired@example.com");
		assertThat(back.getFirstName()).isEqualTo("Wired");
		assertThat(back.getLastName()).isEqualTo("Bean");
		assertThat(back.isAdmin()).isFalse();
		assertThat(back.getCreatedAt()).isNotNull();
		assertThat(back.getUpdatedAt()).isNotNull();
	}

	@Test
	void list_mapping_both_ways() {
		User u1 = new User().setId(1L).setEmail("a@ex.com").setFirstName("A").setLastName("One").setPassword("x")
				.setAdmin(false).setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));

		User u2 = new User().setId(2L).setEmail("b@ex.com").setFirstName("B").setLastName("Two").setPassword("y")
				.setAdmin(true).setCreatedAt(LocalDateTime.parse("2024-02-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-02-02T00:00:00"));

		List<UserDto> dtos = mapper.toDto(List.of(u1, u2));
		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getEmail()).isEqualTo("a@ex.com");
		assertThat(dtos.get(1).isAdmin()).isTrue();

		List<User> back = mapper.toEntity(dtos);
		assertThat(back).hasSize(2);
		assertThat(back.get(0).getEmail()).isEqualTo("a@ex.com");
		assertThat(back.get(1).isAdmin()).isTrue();
	}
}
