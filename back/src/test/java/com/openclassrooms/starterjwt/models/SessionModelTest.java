package com.openclassrooms.starterjwt.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
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
class SessionModelTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator real = factory.getValidator();
		validator = spy(real);
	}

	@Test
	void builder_shouldCreateSessionWithAllFields() {
		Teacher teacher = new Teacher();
		User user1 = new User();
		User user2 = new User();

		Date date = new Date();
		LocalDateTime now = LocalDateTime.now();

		Session session = Session.builder().id(1L).name("Yoga Class").date(date).description("A relaxing yoga session")
				.teacher(teacher).users(Arrays.asList(user1, user2)).createdAt(now.minusDays(1)).updatedAt(now).build();

		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getName()).isEqualTo("Yoga Class");
		assertThat(session.getDate()).isEqualTo(date);
		assertThat(session.getDescription()).isEqualTo("A relaxing yoga session");
		assertThat(session.getTeacher()).isEqualTo(teacher);
		assertThat(session.getUsers()).containsExactly(user1, user2);
		assertThat(session.getCreatedAt()).isEqualTo(now.minusDays(1));
		assertThat(session.getUpdatedAt()).isEqualTo(now);
	}

	@Test
	void equalsAndHashCode_shouldBeBasedOnId() {
		Session s1 = Session.builder().id(1L).name("A").build();
		Session s2 = Session.builder().id(1L).name("B").build();
		Session s3 = Session.builder().id(2L).name("A").build();

		assertThat(s1).isEqualTo(s2);
		assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
		assertThat(s1).isNotEqualTo(s3);
	}

	@Test
	void toString_shouldContainClassNameAndFields() {
		Session session = Session.builder().id(5L).name("Meditation").description("Mindfulness practice")
				.date(new Date()).build();

		String result = session.toString();
		assertThat(result).contains("Meditation");
		assertThat(result).contains("Mindfulness practice");
	}

	@Test
	void validation_shouldDetectInvalidFields_withMockitoSpy() {
		Session invalid = Session.builder().id(1L).name("").description(null).date(null).build();

		Set<ConstraintViolation<Session>> violations = validator.validate(invalid);

		verify(validator, atLeastOnce()).validate(invalid);

		assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString)
				.contains("name", "date", "description");
	}

	@Test
	void validation_shouldPassWithValidFields_withMockitoSpy() {
		Session valid = Session.builder().id(2L).name("Valid session").description("A valid description")
				.date(new Date()).build();

		Set<ConstraintViolation<Session>> violations = validator.validate(valid);

		verify(validator, atLeastOnce()).validate(valid);
		assertThat(violations).isEmpty();
	}

	@Test
	void validation_name_lengthBoundaries_withMockitoSpy() {
		String name50 = "x".repeat(50);
		String name51 = "x".repeat(51);

		Session ok = Session.builder().name(name50).description("desc").date(new Date()).build();

		Session ko = Session.builder().name(name51).description("desc").date(new Date()).build();

		Set<ConstraintViolation<Session>> vOk = validator.validate(ok);
		Set<ConstraintViolation<Session>> vKo = validator.validate(ko);

		ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
		verify(validator, atLeastOnce()).validate(captor.capture());
		assertThat(captor.getAllValues()).isNotEmpty();

		assertThat(vOk).isEmpty();
		assertThat(vKo).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
	}

	@Test
	void validation_description_lengthBoundaries_withMockitoSpy() {
		String d2500 = "x".repeat(2500);
		String d2501 = "x".repeat(2501);

		Session ok = Session.builder().name("ok").description(d2500).date(new Date()).build();

		Session ko = Session.builder().name("ok").description(d2501).date(new Date()).build();

		Set<ConstraintViolation<Session>> vOk = validator.validate(ok);
		Set<ConstraintViolation<Session>> vKo = validator.validate(ko);

		verify(validator, atLeastOnce()).validate(any(Session.class));

		assertThat(vOk).isEmpty();
		assertThat(vKo).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
	}

	@Test
	void validation_blankName_isInvalid_withMockitoSpy() {
		Session s = Session.builder().name("  ").description("desc").date(new Date()).build();

		Set<ConstraintViolation<Session>> violations = validator.validate(s);

		verify(validator, atLeastOnce()).validate(s);

		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
	}

	@Test
	void equals_isReflexive_true() {
		Session s = Session.builder().id(10L).name("X").build();
		assertThat(s.equals(s)).isTrue();
	}

	@Test
	void equals_withNullObject_false() {
		Session s = Session.builder().id(10L).name("X").build();
		assertThat(s.equals(null)).isFalse();
	}

	@Test
	void equals_withDifferentType_false() {
		Session s = Session.builder().id(10L).name("X").build();
		assertThat(s.equals("not a Session")).isFalse();
	}

	@Test
	void equals_bothIdsNull_true() {
		Session s1 = Session.builder().name("A").build();
		Session s2 = Session.builder().name("B").build();
		assertThat(s1).isEqualTo(s2);
		assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
	}

	@Test
	void equals_idNull_vs_idNonNull_false() {
		Session s1 = Session.builder().name("A").build();
		Session s2 = Session.builder().id(1L).name("A").build();
		assertThat(s1).isNotEqualTo(s2);
	}

	static class WeirdSession extends Session {
		@Override
		public boolean canEqual(Object other) {
			return false;
		}
	}

	@Test
	void equals_parentVsChild_canEqualFalse_branchCovered() {
		Session parent = Session.builder().id(1L).name("A").build();
		WeirdSession child = new WeirdSession();
		child.setId(1L);
		child.setName("A");
		assertThat(parent.equals(child)).isFalse();
	}

}
