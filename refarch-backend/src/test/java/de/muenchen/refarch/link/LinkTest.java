package de.muenchen.refarch.link;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LinkTest {

    private static final String TEST_URL = "https://example.com";
    private static final String TEST_NAME = "Example Link";
    private static final String TEST_FA_ICON = "fa-link";
    private static final String TEST_MDI_ICON = "mdi-link";
    private static final String TEST_TYPE = "navigation";
    private static final String URL_FIELD = "url";
    private static final String SCOPE_FIELD = "scope";
    private static final String MUST_NOT_BE_BLANK = "must not be blank";
    private static final String MUST_NOT_BE_NULL = "must not be null";
    private static final String TEST_NEW_URL = "https://test.com";

    private static ValidatorFactory validatorFactory;
    private Validator validator;
    private final Link link = new Link();

    @BeforeAll
    static void setUpClass() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void tearDownClass() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        validator = validatorFactory.getValidator();

        link.setUrl(TEST_URL);
        link.setName(TEST_NAME);
        link.setScope(LinkScope.EXTERNAL);
        link.setFontAwesomeIcon(TEST_FA_ICON);
        link.setMdiIcon(TEST_MDI_ICON);
        link.setType(TEST_TYPE);
    }

    @Test
    void shouldPassValidationWithValidFields() {
        // Act
        final Set<ConstraintViolation<Link>> violations = validator.validate(link);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenUrlIsNull() {
        // Arrange
        link.setUrl(null);

        // Act
        final Set<ConstraintViolation<Link>> violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> URL_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> violation.getMessage().contains(MUST_NOT_BE_BLANK));
    }

    @Test
    void shouldFailValidationWhenUrlIsBlank() {
        // Arrange
        link.setUrl("");

        // Act
        final Set<ConstraintViolation<Link>> violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> URL_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> violation.getMessage().contains(MUST_NOT_BE_BLANK));
    }

    @Test
    void shouldFailValidationWhenScopeIsNull() {
        // Arrange
        link.setScope(null);

        // Act
        final Set<ConstraintViolation<Link>> violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> SCOPE_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> violation.getMessage().contains(MUST_NOT_BE_NULL));
    }

    @Test
    void shouldPassValidationWhenOptionalFieldsAreNull() {
        // Arrange
        link.setName(null);
        link.setFontAwesomeIcon(null);
        link.setMdiIcon(null);
        link.setType(null);

        // Act
        final Set<ConstraintViolation<Link>> violations = validator.validate(link);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldReturnUrlWhenGettingLink() {
        // Arrange
        link.setUrl(TEST_NEW_URL);

        // Act
        final String result = link.getLink();

        // Assert
        assertThat(result).isEqualTo(TEST_NEW_URL);
    }

    @Test
    void shouldUpdateUrlWhenSettingUrl() {
        // Act
        link.setUrl(TEST_NEW_URL);

        // Assert
        assertThat(link.getUrl()).isEqualTo(TEST_NEW_URL);
        assertThat(link.getLink()).isEqualTo(TEST_NEW_URL);
    }
}
