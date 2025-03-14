package de.muenchen.refarch.language;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageTest {

    private Validator validator;
    private Language language;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("fa-flag-usa");
        language.setMdiIcon("mdi-flag");
    }

    @Test
    void whenAllFieldsAreValid_ShouldHaveNoViolations() {
        // Act
        var violations = validator.validate(language);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNull_ShouldHaveViolation() {
        // Arrange
        language.setName(null);

        // Act
        var violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("name"))
                .allMatch(violation -> violation.getMessage().equals("must not be blank"));
    }

    @Test
    void whenAbbreviationIsNull_ShouldHaveViolation() {
        // Arrange
        language.setAbbreviation(null);

        // Act
        var violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("abbreviation"))
                .allMatch(violation -> violation.getMessage().equals("must not be blank"));
    }

    @Test
    void whenFontAwesomeIconIsNull_ShouldHaveViolation() {
        // Arrange
        language.setFontAwesomeIcon(null);

        // Act
        var violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("fontAwesomeIcon"))
                .allMatch(violation -> violation.getMessage().equals("must not be blank"));
    }

    @Test
    void whenMdiIconIsNull_ShouldHaveViolation() {
        // Arrange
        language.setMdiIcon(null);

        // Act
        var violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("mdiIcon"))
                .allMatch(violation -> violation.getMessage().equals("must not be blank"));
    }
}
