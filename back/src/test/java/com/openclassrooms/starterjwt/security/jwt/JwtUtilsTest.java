package com.openclassrooms.starterjwt.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

	private JwtUtils jwtUtils;

	private static final String TEST_SECRET = "testSecretValue1234567890";
	private static final int ONE_HOUR_MS = 3_600_000;

	@BeforeEach
	void setUp() {
		jwtUtils = new JwtUtils();
		ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
		ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", ONE_HOUR_MS);
	}

	@Test
	void generateJwtToken_shouldCreateSignedTokenWithSubjectAndExpiry() {
		// given
		Authentication authentication = mock(Authentication.class);
		UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

		when(userDetails.getUsername()).thenReturn("john.doe");
		when(authentication.getPrincipal()).thenReturn(userDetails);

		// when
		String token = jwtUtils.generateJwtToken(authentication);

		// then
		assertNotNull(token, "Le token ne doit pas être nul");

		String subject = Jwts.parser().setSigningKey(TEST_SECRET).parseClaimsJws(token).getBody().getSubject();

		assertEquals("john.doe", subject, "Le subject doit être le username");
	}

	@Test
	void getUserNameFromJwtToken_shouldReturnSubject() {
		// given
		String token = Jwts.builder().setSubject("alice").setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(SignatureAlgorithm.HS512, TEST_SECRET).compact();

		// when
		String username = jwtUtils.getUserNameFromJwtToken(token);

		// then
		assertEquals("alice", username);
	}

	@Test
	void validateJwtToken_shouldReturnTrueForValidToken() {
		String valid = Jwts.builder().setSubject("valid.user").setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(SignatureAlgorithm.HS512, TEST_SECRET).compact();

		assertTrue(jwtUtils.validateJwtToken(valid));
	}

	@Test
	void validateJwtToken_shouldReturnFalseForExpiredToken() {
		String expired = Jwts.builder().setSubject("expired.user")
				.setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
				.setExpiration(new Date(System.currentTimeMillis() - 60_000))
				.signWith(SignatureAlgorithm.HS512, TEST_SECRET).compact();

		assertFalse(jwtUtils.validateJwtToken(expired));
	}

	@Test
	void validateJwtToken_shouldReturnFalseForInvalidSignature() {
		String otherSecret = "someOtherSecret";
		String badSignature = Jwts.builder().setSubject("user").setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(SignatureAlgorithm.HS512, otherSecret).compact();

		assertFalse(jwtUtils.validateJwtToken(badSignature));
	}

	@Test
	void validateJwtToken_shouldReturnFalseForMalformedToken() {
		String malformed = "not.a.jwt";
		assertFalse(jwtUtils.validateJwtToken(malformed));
	}

	@Test
	void validateJwtToken_shouldReturnFalseForEmptyString() {
		assertFalse(jwtUtils.validateJwtToken(""));
	}
}
