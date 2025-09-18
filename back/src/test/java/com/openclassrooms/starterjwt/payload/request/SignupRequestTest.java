package com.openclassrooms.starterjwt.payload.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SignupRequestTest {

	private SignupRequest full(String email, String first, String last, String pwd) {
		SignupRequest s = new SignupRequest();
		s.setEmail(email);
		s.setFirstName(first);
		s.setLastName(last);
		s.setPassword(pwd);
		return s;
	}

	@Test
	void signupRequest_equals_hashCode_toString() {
		SignupRequest a = new SignupRequest();
		a.setEmail("a@b.com");
		a.setFirstName("Ana");
		a.setLastName("Bell");
		a.setPassword("secret123");

		SignupRequest b = new SignupRequest();
		b.setEmail("a@b.com");
		b.setFirstName("Ana");
		b.setLastName("Bell");
		b.setPassword("secret123");

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertTrue(a.toString().contains("a@b.com"));
		assertEquals("Ana", a.getFirstName());
	}

	@Test
	void equals_isReflexive() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		assertTrue(a.equals(a));
	}

	@Test
	void equals_withNull_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		assertFalse(a.equals(null));
	}

	@Test
	void equals_withDifferentType_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		assertFalse(a.equals(new Object()));
	}

	static class SignupRequestChild extends SignupRequest {
	}

	@Test
	void equals_parentVsChild_areEqualWithLombok() {
		SignupRequest parent = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequestChild child = new SignupRequestChild();
		child.setEmail("a@b.com");
		child.setFirstName("Ana");
		child.setLastName("Bell");
		child.setPassword("secret123");

		assertTrue(parent.equals(child));
		assertTrue(child.equals(parent));
	}

	@Test
	void equals_sameFieldValues_isTrue_andHashMatches() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", "Bell", "secret123");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equals_differentEmail_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("c@d.com", "Ana", "Bell", "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void equals_nullVsNonNullField_isFalse() {
		SignupRequest a = full(null, "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", "Bell", "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void canEqual_behavior() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		assertTrue(a.canEqual(full("a@b.com", "Ana", "Bell", "secret123")));
		assertFalse(a.canEqual(new Object()));
	}

	@Test
	void toString_containsFields() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		assertTrue(a.toString().contains("a@b.com"));
	}

	@Test
	void equals_bothNullEmails_areEqual() {
		SignupRequest a = full(null, "Ana", "Bell", "secret123");
		SignupRequest b = full(null, "Ana", "Bell", "secret123");
		assertEquals(a, b);
	}

	@Test
	void equals_firstName_diff_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ann", "Bell", "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void equals_firstName_nullVsNonNull_isFalse() {
		SignupRequest a = full("a@b.com", null, "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", "Bell", "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void equals_lastName_diff_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", "Bello", "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void equals_lastName_nonNullVsNull_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", null, "secret123");
		assertNotEquals(a, b);
	}

	@Test
	void equals_password_diff_isFalse() {
		SignupRequest a = full("a@b.com", "Ana", "Bell", "secret123");
		SignupRequest b = full("a@b.com", "Ana", "Bell", "secret456");
		assertNotEquals(a, b);
	}

	@Test
	void equals_allFieldsNull_areEqual() {
		SignupRequest a = full(null, null, null, null);
		SignupRequest b = full(null, null, null, null);
		assertEquals(a, b);
		assertEquals(b, a);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void equals_bothNullFirstName_areEqual() {
		SignupRequest a = full("e@e.com", null, "L", "p");
		SignupRequest b = full("e@e.com", null, "L", "p");
		assertEquals(a, b);
	}

	@Test
	void equals_bothNullLastName_areEqual() {
		SignupRequest a = full("e@e.com", "F", null, "p");
		SignupRequest b = full("e@e.com", "F", null, "p");
		assertEquals(a, b);
	}

	@Test
	void equals_bothNullPassword_areEqual() {
		SignupRequest a = full("e@e.com", "F", "L", null);
		SignupRequest b = full("e@e.com", "F", "L", null);
		assertEquals(a, b);
	}

	@Test
	void equals_password_nullVsNonNull_isFalse() {
		SignupRequest a = full("e@e.com", "F", "L", null);
		SignupRequest b = full("e@e.com", "F", "L", "pwd");
		assertNotEquals(a, b);
	}

	@Test
	void equals_transitivity_holds() {
		SignupRequest a = full("e@e.com", "F", "L", "pwd");
		SignupRequest b = full("e@e.com", "F", "L", "pwd");
		SignupRequest c = full("e@e.com", "F", "L", "pwd");
		assertEquals(a, b);
		assertEquals(b, c);
		assertEquals(a, c);
	}

	@Test
	void hashCode_isConsistent_acrossCalls() {
		SignupRequest a = full("e@e.com", "F", "L", "pwd");
		int h1 = a.hashCode();
		int h2 = a.hashCode();
		assertEquals(h1, h2);
	}

	@Test
	void hashCode_differs_whenAFieldDiffers_email() {
		SignupRequest a = full("a@b.com", "F", "L", "pwd");
		SignupRequest b = full("x@y.com", "F", "L", "pwd");
		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void hashCode_handlesNullFields() {
		SignupRequest a = full(null, "F", "L", "pwd");
		SignupRequest b = full(null, "F", "L", "pwd");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void toString_containsPresentFieldsAndHandlesNulls() {
		SignupRequest a = full("e@e.com", null, "L", null);
		String s = a.toString();
		assertTrue(s.contains("e@e.com"));

	}

}
