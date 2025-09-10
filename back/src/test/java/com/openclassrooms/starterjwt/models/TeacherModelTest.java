package com.openclassrooms.starterjwt.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
class TeacherModelTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator real = factory.getValidator();
		validator = spy(real);
	}

	@Test
	void builder_shouldCreateTeacherWithAllFields() {
		LocalDateTime now = LocalDateTime.now();

		Teacher teacher = Teacher.builder().id(1L).firstName("Ada").lastName("Lovelace").createdAt(now.minusDays(1))
				.updatedAt(now).build();

		assertThat(teacher.getId()).isEqualTo(1L);
		assertThat(teacher.getFirstName()).isEqualTo("Ada");
		assertThat(teacher.getLastName()).isEqualTo("Lovelace");
		assertThat(teacher.getCreatedAt()).isEqualTo(now.minusDays(1));
		assertThat(teacher.getUpdatedAt()).isEqualTo(now);
	}

	@Test
	void equalsAndHashCode_shouldBeBasedOnId() {
		Teacher t1 = Teacher.builder().id(1L).firstName("A").lastName("B").build();
		Teacher t2 = Teacher.builder().id(1L).firstName("X").lastName("Y").build();
		Teacher t3 = Teacher.builder().id(2L).firstName("A").lastName("B").build();

		assertThat(t1).isEqualTo(t2);
		assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
		assertThat(t1).isNotEqualTo(t3);
	}

	@Test
	void toString_shouldContainFieldValues() {
		Teacher teacher = Teacher.builder().id(5L).firstName("Alan").lastName("Turing").build();

		String result = teacher.toString();
		assertThat(result).contains("Alan");
		assertThat(result).contains("Turing");
	}

	@Test
	void validation_shouldDetectInvalidFields_withMockitoSpy() {
		Teacher invalid = Teacher.builder().id(1L).firstName("").lastName(null).build();

		Set<ConstraintViolation<Teacher>> violations = validator.validate(invalid);

		verify(validator, atLeastOnce()).validate(invalid);

		assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString)
				.contains("firstName", "lastName");
	}

	@Test
	void validation_shouldPassWithValidFields_withMockitoSpy() {
		Teacher valid = Teacher.builder().id(2L).firstName("Grace").lastName("Hopper").build();

		Set<ConstraintViolation<Teacher>> violations = validator.validate(valid);

		verify(validator, atLeastOnce()).validate(valid);
		assertThat(violations).isEmpty();
	}

	@Test
	void validation_firstName_lengthBoundaries_withMockitoSpy() {
		String n20 = "x".repeat(20);
		String n21 = "x".repeat(21);

		Teacher ok = Teacher.builder().firstName(n20).lastName("Doe").build();
		Teacher ko = Teacher.builder().firstName(n21).lastName("Doe").build();

		Set<ConstraintViolation<Teacher>> vOk = validator.validate(ok);
		Set<ConstraintViolation<Teacher>> vKo = validator.validate(ko);
		ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
		verify(validator, atLeastOnce()).validate(captor.capture());
		assertThat(captor.getAllValues()).isNotEmpty();

		assertThat(vOk).isEmpty();
		assertThat(vKo).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@Test
	void validation_lastName_lengthBoundaries_withMockitoSpy() {
		String n20 = "x".repeat(20);
		String n21 = "x".repeat(21);

		Teacher ok = Teacher.builder().lastName(n20).firstName("John").build();
		Teacher ko = Teacher.builder().lastName(n21).firstName("John").build();

		Set<ConstraintViolation<Teacher>> vOk = validator.validate(ok);
		Set<ConstraintViolation<Teacher>> vKo = validator.validate(ko);

		verify(validator, atLeastOnce()).validate(any(Teacher.class));

		assertThat(vOk).isEmpty();
		assertThat(vKo).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@Test
	void validation_blankNames_areInvalid_withMockitoSpy() {
		Teacher t = Teacher.builder().firstName("  ").lastName("\t").build();

		Set<ConstraintViolation<Teacher>> violations = validator.validate(t);

		verify(validator, atLeastOnce()).validate(t);

		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"))
				.anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
	}

	@Test
	void equals_isReflexive_true() {
		Teacher t = Teacher.builder().id(10L).firstName("Ada").lastName("Lovelace").build();
		assertThat(t.equals(t)).isTrue();
	}

	@Test
	void equals_withNullObject_false() {
		Teacher t = Teacher.builder().id(10L).firstName("Ada").lastName("Lovelace").build();
		assertThat(t.equals(null)).isFalse();
	}

	@Test
	void equals_withDifferentType_false() {
		Teacher t = Teacher.builder().id(10L).firstName("Ada").lastName("Lovelace").build();
		assertThat(t.equals("not a Teacher")).isFalse();
	}

	@Test
	void equals_bothIdsNull_true() {
		Teacher t1 = Teacher.builder().firstName("A").lastName("B").build();
		Teacher t2 = Teacher.builder().firstName("X").lastName("Y").build();
		assertThat(t1).isEqualTo(t2);
		assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
	}

	@Test
	void equals_idNull_vs_idNonNull_false() {
		Teacher t1 = Teacher.builder().firstName("A").lastName("B").build();
		Teacher t2 = Teacher.builder().id(1L).firstName("A").lastName("B").build();
		assertThat(t1).isNotEqualTo(t2);
	}

}
