package com.openclassrooms.starterjwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class SpringBootSecurityJwtApplicationTest {

	@Test
	void main_callsSpringApplicationRun() {
		try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
			mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class))).thenReturn(null);

			SpringBootSecurityJwtApplication.main(new String[] {});

			mocked.verify(() -> SpringApplication.run(SpringBootSecurityJwtApplication.class, new String[] {}),
					times(1));
		}
	}
}
