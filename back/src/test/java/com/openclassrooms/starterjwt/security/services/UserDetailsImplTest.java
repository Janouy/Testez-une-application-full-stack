package com.openclassrooms.starterjwt.security.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

class UserDetailsImplTest {

	@Test
	void builder_shouldBuildAllFields_andGettersWork() {
		UserDetailsImpl u = UserDetailsImpl.builder().id(1L).username("alice").firstName("Alice").lastName("Wonder")
				.admin(true).password("secret").build();

		assertThat(u.getId()).isEqualTo(1L);
		assertThat(u.getUsername()).isEqualTo("alice");
		assertThat(u.getFirstName()).isEqualTo("Alice");
		assertThat(u.getLastName()).isEqualTo("Wonder");
		assertThat(u.getAdmin()).isTrue();
		assertThat(u.getPassword()).isEqualTo("secret");
	}

	@Test
	void getAuthorities_shouldReturnEmptySet() {
		UserDetailsImpl u = UserDetailsImpl.builder().build();

		Collection<? extends GrantedAuthority> authorities = u.getAuthorities();

		assertThat(authorities).isNotNull().isEmpty();
	}

	@Test
	void userDetailsFlags_shouldAllBeTrue() {
		UserDetailsImpl u = UserDetailsImpl.builder().build();

		assertThat(u.isAccountNonExpired()).isTrue();
		assertThat(u.isAccountNonLocked()).isTrue();
		assertThat(u.isCredentialsNonExpired()).isTrue();
		assertThat(u.isEnabled()).isTrue();
	}

	@Test
	void equals_shouldUseOnlyId() {
		UserDetailsImpl u1 = UserDetailsImpl.builder().id(10L).username("a").build();
		UserDetailsImpl u2 = UserDetailsImpl.builder().id(10L).username("b").build();
		UserDetailsImpl u3 = UserDetailsImpl.builder().id(11L).username("a").build();

		assertThat(u1).isEqualTo(u2);
		assertThat(u1).isNotEqualTo(u3);
		assertThat(u1).isEqualTo(u1);
		assertThat(u1.equals(null)).isFalse();
		assertThat(u1.equals("foo")).isFalse();
	}

	@Test
	void jsonSerialization_shouldOmitPassword() throws Exception {
		ObjectMapper om = new ObjectMapper();
		UserDetailsImpl u = UserDetailsImpl.builder().id(1L).username("alice").password("secret").build();

		String json = om.writeValueAsString(u);

		assertThat(json).contains("\"id\":1").contains("\"username\":\"alice\"").doesNotContain("password");
	}
}
