package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostsUsersTest {

    private Validator validator;
    private PostsUsers postsUsers;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        postsUsers = new PostsUsers();
        postsUsers.setId(UUID.randomUUID());
        postsUsers.setPostLinkId(UUID.randomUUID());
        postsUsers.setUser(user);
    }

    @Test
    void whenAllFieldsAreValid_ShouldHaveNoViolations() {
        // Act
        var violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void whenPostLinkIdIsNull_ShouldHaveViolation() {
        // Arrange
        postsUsers.setPostLinkId(null);

        // Act
        var violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("postLinkId"))
                .allMatch(violation -> violation.getMessage().equals("must not be null"));
    }

    @Test
    void whenUserIsNull_ShouldHaveViolation() {
        // Arrange
        postsUsers.setUser(null);

        // Act
        var violations = validator.validate(postsUsers);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("user"))
                .allMatch(violation -> violation.getMessage().equals("must not be null"));
    }
}
