package com.openclassrooms.starterjwt.payload.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageResponseTest {

	@Test
	void constructor_setsMessage() {
		MessageResponse r = new MessageResponse("Hello");
		assertThat(r.getMessage()).isEqualTo("Hello");
	}

	@Test
	void gettersSetters_work() {
		MessageResponse r = new MessageResponse("X");
		r.setMessage("Updated");
		assertThat(r.getMessage()).isEqualTo("Updated");
	}
}
