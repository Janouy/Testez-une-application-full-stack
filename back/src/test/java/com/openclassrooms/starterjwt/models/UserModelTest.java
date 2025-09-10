package com.openclassrooms.starterjwt.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserModelTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator real = factory.getValidator();
		validator = spy(real);
	}

	@Test
	void builder_shouldCreateUserWithAllFields() {
		LocalDateTime now = LocalDateTime.now();

		User user = User.builder().id(1L).email("test@example.com").firstName("Alice").lastName("Wonder")
				.password("secret123").admin(true).createdAt(now.minusDays(1)).updatedAt(now).build();

		assertThat(user.getId()).isEqualTo(1L);
		assertThat(user.getEmail()).isEqualTo("test@example.com");
		assertThat(user.getFirstName()).isEqualTo("Alice");
		assertThat(user.getLastName()).isEqualTo("Wonder");
		assertThat(user.getPassword()).isEqualTo("secret123");
		assertThat(user.isAdmin()).isTrue();
		assertThat(user.getCreatedAt()).isEqualTo(now.minusDays(1));
		assertThat(user.getUpdatedAt()).isEqualTo(now);
	}

	@Test
	void requiredArgsConstructor_shouldCreateUserWithNonNullFields() {
		User user = new User("user@example.com", "Wonder", "Alice", "pwd123", false);
		assertThat(user.getEmail()).isEqualTo("user@example.com");
		assertThat(user.getLastName()).isEqualTo("Wonder");
		assertThat(user.getFirstName()).isEqualTo("Alice");
		assertThat(user.getPassword()).isEqualTo("pwd123");
		assertThat(user.isAdmin()).isFalse();
	}

	@Test
	void equalsAndHashCode_shouldBeBasedOnId() {
		User u1 = User.builder().id(1L).email("a@a.com").lastName("A").firstName("A").password("x").admin(false)
				.build();
		User u2 = User.builder().id(1L).email("b@b.com").lastName("B").firstName("B").password("y").admin(true).build();
		User u3 = User.builder().id(2L).email("c@c.com").lastName("C").firstName("C").password("z").admin(false)
				.build();

		assertThat(u1).isEqualTo(u2);
		assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
		assertThat(u1).isNotEqualTo(u3);
	}

	@Test
	void toString_shouldContainFieldValues() {
		User user = User.builder().id(42L).email("mail@example.com").firstName("Alice").lastName("Wonder")
				.password("pwd").admin(true).build();

		String result = user.toString();
		assertThat(result).contains("mail@example.com");
		assertThat(result).contains("Alice");
		assertThat(result).contains("Wonder");
	}

	@Test
	void validation_shouldDetectInvalidEmailAndSizeConstraints_withMockitoSpy() {
		User invalid = User.builder().email("invalid-email").firstName("ANameThatIsWayTooLongForConstraint")
				.lastName("").password("").admin(false).build();

		Set<ConstraintViolation<User>> violations = validator.validate(invalid);

		verify(validator, atLeastOnce()).validate(invalid);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("email", "firstName");
	}

	@Test
	void validation_shouldPassWithValidFields_withMockitoSpy() {
		User valid = User.builder().email("mail@example.com").firstName("Alice").lastName("Wonder")
				.password("safePassword").admin(false).build();

		Set<ConstraintViolation<User>> violations = validator.validate(valid);

		verify(validator, atLeastOnce()).validate(valid);
		assertThat(violations).isEmpty();
	}

	@Test
	void validation_names_and_password_lengthBoundaries_withMockitoSpy() {
		String n20 = "x".repeat(20), n21 = "x".repeat(21);
		String p120 = "x".repeat(120), p121 = "x".repeat(121);

		User ok = User.builder().email("a@a.fr").firstName(n20).lastName(n20).password(p120).admin(false).build();
		User ko1 = User.builder().email("a@a.fr").firstName(n21).lastName("B").password("p").admin(false).build();
		User ko2 = User.builder().email("a@a.fr").firstName("A").lastName(n21).password("p").admin(false).build();
		User ko3 = User.builder().email("a@a.fr").firstName("A").lastName("B").password(p121).admin(false).build();

		Set<ConstraintViolation<User>> vOk = validator.validate(ok);
		Set<ConstraintViolation<User>> v1 = validator.validate(ko1);
		Set<ConstraintViolation<User>> v2 = validator.validate(ko2);
		Set<ConstraintViolation<User>> v3 = validator.validate(ko3);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(validator, atLeastOnce()).validate(userCaptor.capture());
		assertThat(userCaptor.getAllValues()).isNotEmpty();

		assertThat(vOk).isEmpty();
		assertThat(v1).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
		assertThat(v2).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
		assertThat(v3).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
	}

	@Test
	void equals_nullAndNullIds_areEqual() {
		User u1 = User.builder().email("a@a.com").firstName("A").lastName("B").password("x").admin(false).build();
		User u2 = User.builder().email("b@b.com").firstName("C").lastName("D").password("y").admin(true).build();

		assertThat(u1).isEqualTo(u2);
	}

	@Test
	void equals_withNullObject_isFalse() {
		User u1 = User.builder().id(1L).email("a@a.com").firstName("A").lastName("B").password("x").admin(false)
				.build();
		assertThat(u1.equals(null)).isFalse();
	}

	@Test
	void equals_withDifferentType_isFalse() {
		User u1 = User.builder().id(1L).email("a@a.com").firstName("A").lastName("B").password("x").admin(false)
				.build();
		assertThat(u1.equals("a string")).isFalse();
	}

	@Test
	void builder_toString_isCovered() {
		String s = User.builder().email("a@a.com").firstName("Ana").lastName("Bell").password("xxx").admin(true)
				.toString();
		assertThat(s).contains("User.UserBuilder").contains("email=");
	}

	@Test
	void builder_email_null_throwsNpe() {
		assertThatThrownBy(() -> User.builder().email(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void builder_firstName_null_throwsNpe() {
		assertThatThrownBy(() -> User.builder().firstName(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void builder_lastName_null_throwsNpe() {
		assertThatThrownBy(() -> User.builder().lastName(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void builder_password_null_throwsNpe() {
		assertThatThrownBy(() -> User.builder().password(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void setEmail_null_throwsNpe() {
		assertThatThrownBy(() -> new User().setEmail(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void setFirstName_null_throwsNpe() {
		assertThatThrownBy(() -> new User().setFirstName(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void setLastName_null_throwsNpe() {
		assertThatThrownBy(() -> new User().setLastName(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void setPassword_null_throwsNpe() {
		assertThatThrownBy(() -> new User().setPassword(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void ctor_required_email_null_throwsNpe() {
		assertThatThrownBy(() -> new User(null, "L", "F", "p", false)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void ctor_required_lastName_null_throwsNpe() {
		assertThatThrownBy(() -> new User("e@e.com", null, "F", "p", false)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void ctor_required_firstName_null_throwsNpe() {
		assertThatThrownBy(() -> new User("e@e.com", "L", null, "p", false)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void ctor_required_password_null_throwsNpe() {
		assertThatThrownBy(() -> new User("e@e.com", "L", "F", null, false)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void ctor_all_email_null_throwsNpe() {
		var now = java.time.LocalDateTime.now();
		assertThatThrownBy(() -> new User(1L, null, "L", "F", "p", false, now, now))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void equals_bothIdsNull_true_and_hashCode_equal() {
		User a = User.builder().email("a@a").lastName("L").firstName("F").password("p").admin(false).build();
		User b = User.builder().email("b@b").lastName("X").firstName("Y").password("q").admin(true).build();
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	void hashCode_idNonNull_branch() {
		User a = User.builder().id(1L).email("a@a").lastName("L").firstName("F").password("p").admin(false).build();
		assertThat(a.hashCode()).isNotZero();
	}

}
