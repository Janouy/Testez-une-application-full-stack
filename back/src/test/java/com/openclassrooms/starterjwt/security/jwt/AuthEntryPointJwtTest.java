package com.openclassrooms.starterjwt.security.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

	private final AuthEntryPointJwt entryPoint = new AuthEntryPointJwt();
	private final ObjectMapper mapper = new ObjectMapper();

	@Captor
	ArgumentCaptor<Integer> statusCaptor;
	@Captor
	ArgumentCaptor<String> contentTypeCaptor;

	private static class CapturingServletOutputStream extends ServletOutputStream {
		private final ByteArrayOutputStream bos;

		CapturingServletOutputStream(ByteArrayOutputStream bos) {
			this.bos = bos;
		}

		@Override
		public void write(int b) throws IOException {
			bos.write(b);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
		}
	}

	@Test
	void commence_shouldReturn401_andJsonBody_withMessageAndPath() throws Exception {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(request.getServletPath()).thenReturn("/api/courses/42");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ServletOutputStream sos = new CapturingServletOutputStream(bos);
		when(response.getOutputStream()).thenReturn(sos);

		AuthenticationException ex = new BadCredentialsException("Bad credentials");

		entryPoint.commence(request, response, ex);

		verify(response).setStatus(statusCaptor.capture());
		assertEquals(401, statusCaptor.getValue());

		verify(response, atLeastOnce()).setContentType(contentTypeCaptor.capture());
		assertTrue(contentTypeCaptor.getValue().startsWith(MediaType.APPLICATION_JSON_VALUE));

		String json = bos.toString();
		Map<String, Object> body = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
		assertEquals(401, (Integer) body.get("status"));
		assertEquals("Unauthorized", body.get("error"));
		assertEquals("Bad credentials", body.get("message"));
		assertEquals("/api/courses/42", body.get("path"));
	}

	@Test
	void commence_shouldHandleNullExceptionMessage() throws Exception {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(request.getServletPath()).thenReturn("/auth/login");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ServletOutputStream sos = new CapturingServletOutputStream(bos);
		when(response.getOutputStream()).thenReturn(sos);

		AuthenticationException ex = new BadCredentialsException(null);

		entryPoint.commence(request, response, ex);

		verify(response).setStatus(statusCaptor.capture());
		assertEquals(401, statusCaptor.getValue());
		verify(response, atLeastOnce()).setContentType(contentTypeCaptor.capture());
		assertTrue(contentTypeCaptor.getValue().startsWith(MediaType.APPLICATION_JSON_VALUE));

		String json = bos.toString();
		Map<String, Object> body = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
		assertEquals(401, (Integer) body.get("status"));
		assertEquals("Unauthorized", body.get("error"));
		assertEquals("/auth/login", body.get("path"));

	}

	@Test
	void commence_shouldWriteValidJson() throws Exception {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(request.getServletPath()).thenReturn("/any");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ServletOutputStream sos = new CapturingServletOutputStream(bos);
		when(response.getOutputStream()).thenReturn(sos);

		AuthenticationException ex = new BadCredentialsException("Bad credentials");

		entryPoint.commence(request, response, ex);

		assertDoesNotThrow(() -> mapper.readTree(bos.toString()));
	}
}
