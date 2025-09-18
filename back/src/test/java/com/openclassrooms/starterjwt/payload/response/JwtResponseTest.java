package com.openclassrooms.starterjwt.payload.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtResponseTest {

	@Test
	void constructor_setsAllFields_andTypeDefaultIsBearer() {
		JwtResponse r = new JwtResponse("access-token", 42L, "jdoe", "John", "Doe", true);

		assertThat(r.getToken()).isEqualTo("access-token");
		assertThat(r.getId()).isEqualTo(42L);
		assertThat(r.getUsername()).isEqualTo("jdoe");
		assertThat(r.getFirstName()).isEqualTo("John");
		assertThat(r.getLastName()).isEqualTo("Doe");
		assertThat(r.getAdmin()).isTrue();
		assertThat(r.getType()).isEqualTo("Bearer");
	}

	@Test
	void lombok_gettersSetters_work() {
		JwtResponse r = new JwtResponse("t", 1L, "u", "f", "l", false);

		r.setToken("new-token");
		r.setType("Custom");
		r.setId(7L);
		r.setUsername("newu");
		r.setFirstName("Alice");
		r.setLastName("Smith");
		r.setAdmin(true);

		assertThat(r.getToken()).isEqualTo("new-token");
		assertThat(r.getType()).isEqualTo("Custom");
		assertThat(r.getId()).isEqualTo(7L);
		assertThat(r.getUsername()).isEqualTo("newu");
		assertThat(r.getFirstName()).isEqualTo("Alice");
		assertThat(r.getLastName()).isEqualTo("Smith");
		assertThat(r.getAdmin()).isTrue();
	}
}
