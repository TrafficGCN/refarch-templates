package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostsUsersTest {

    private Validator validator;
    private PostsUsers postsUsers;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();

            final User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("testuser");
            user.setEmail("test@example.com");

            postsUsers = new PostsUsers();
            postsUsers.setId(UUID.randomUUID());
            postsUsers.setPostLinkId(UUID.randomUUID());
            postsUsers.setUser(user);
        }
    }

    @Test
    void shouldValidateValidFields() {
        // Act
        final Set<ConstraintViolation<PostsUsers>> violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectNullPostLinkId() {
        // Arrange
        postsUsers.setPostLinkId(null);

        // Act
        final Set<ConstraintViolation<PostsUsers>> violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> "postLinkId".equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> "must not be null".equals(violation.getMessage()));
    }

    @Test
    void shouldRejectNullUser() {
        // Arrange
        postsUsers.setUser(null);

        // Act
        final Set<ConstraintViolation<PostsUsers>> violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> "user".equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> "must not be null".equals(violation.getMessage()));
    }
}
