package com.openclassrooms.starterjwt.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;

@SpringBootTest(classes = { UserControllerIT.TestApp.class,
		UserControllerIT.MapStructConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "com.openclassrooms.starterjwt")
	static class TestApp {
	}

	@Configuration
	static class MapStructConfig {
		@Bean
		UserMapper userMapper() {
			return Mappers.getMapper(UserMapper.class);
		}
	}

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserService userService;

	private User user(long id, String email, boolean admin) {
		return new User().setId(id).setEmail(email).setFirstName("Alice").setLastName("Doe").setPassword("secret")
				.setAdmin(admin).setCreatedAt(LocalDateTime.parse("2024-01-01T00:00:00"))
				.setUpdatedAt(LocalDateTime.parse("2024-01-02T00:00:00"));
	}

	@Test
	@WithMockUser(username = "whoever@example.com", roles = { "USER" })
	void getById_ok_returns200_andBody() throws Exception {
		Mockito.when(userService.findById(1L)).thenReturn(user(1L, "alice@example.com", true));

		mvc.perform(get("/api/user/{id}", "1").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.email").value("alice@example.com"))
				.andExpect(jsonPath("$.firstName").value("Alice")).andExpect(jsonPath("$.lastName").value("Doe"))
				.andExpect(jsonPath("$.admin").value(true)).andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.updatedAt").exists()).andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	@WithMockUser
	void getById_notFound_returns404() throws Exception {
		Mockito.when(userService.findById(42L)).thenReturn(null);

		mvc.perform(get("/api/user/{id}", "42")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void getById_badRequest_whenIdNotNumeric_returns400() throws Exception {
		mvc.perform(get("/api/user/{id}", "abc")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "alice@example.com", roles = { "USER" })
	void delete_ok_whenAuthenticatedUserMatchesEmail_returns200_andCallsService() throws Exception {
		Mockito.when(userService.findById(1L)).thenReturn(user(1L, "alice@example.com", false));

		mvc.perform(delete("/api/user/{id}", "1").with(csrf())).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(""));

		verify(userService).delete(1L);
	}

	@Test
	@WithMockUser(username = "eve@example.com", roles = { "USER" })
	void delete_unauthorized_whenPrincipalDiffersFromUserEmail_returns401_andNoDelete() throws Exception {
		Mockito.when(userService.findById(1L)).thenReturn(user(1L, "alice@example.com", false));

		mvc.perform(delete("/api/user/{id}", "1").with(csrf())).andDo(print()).andExpect(status().isUnauthorized());

		verify(userService, never()).delete(anyLong());
	}

	@Test
	@WithMockUser(username = "whoever@example.com", roles = { "USER" })
	void delete_notFound_whenUserDoesNotExist_returns404_andNoDelete() throws Exception {
		Mockito.when(userService.findById(9L)).thenReturn(null);

		mvc.perform(delete("/api/user/{id}", "9").with(csrf())).andDo(print()).andExpect(status().isNotFound());

		verify(userService, never()).delete(anyLong());
	}

	@Test
	@WithMockUser
	void delete_badRequest_whenIdNotNumeric_returns400_andNoDelete() throws Exception {
		mvc.perform(delete("/api/user/{id}", "xyz").with(csrf())).andDo(print()).andExpect(status().isBadRequest());

		verify(userService, never()).delete(anyLong());
	}
}
