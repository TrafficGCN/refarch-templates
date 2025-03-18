package de.muenchen.refarch.language;

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

class LanguageTest {

    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String FA_FLAG_USA = "fa-flag-usa";
    private static final String MDI_FLAG = "mdi-flag";
    private static final String MUST_NOT_BE_BLANK = "must not be blank";
    private static final String NAME_FIELD = "name";
    private static final String ABBREVIATION_FIELD = "abbreviation";
    private static final String FONT_AWESOME_ICON_FIELD = "fontAwesomeIcon";
    private static final String MDI_ICON_FIELD = "mdiIcon";

    private static ValidatorFactory validatorFactory;
    private Validator validator;
    private Language language;

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

        language = new Language();
        language.setName(ENGLISH);
        language.setAbbreviation(EN);
        language.setFontAwesomeIcon(FA_FLAG_USA);
        language.setMdiIcon(MDI_FLAG);
    }

    @Test
    void whenAllFieldsAreValid_ShouldHaveNoViolations() {
        // Act
        final Set<ConstraintViolation<Language>> violations = validator.validate(language);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNull_ShouldHaveViolation() {
        // Arrange
        language.setName(null);

        // Act
        final Set<ConstraintViolation<Language>> violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> NAME_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> MUST_NOT_BE_BLANK.equals(violation.getMessage()));
    }

    @Test
    void whenAbbreviationIsNull_ShouldHaveViolation() {
        // Arrange
        language.setAbbreviation(null);

        // Act
        final Set<ConstraintViolation<Language>> violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> ABBREVIATION_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> MUST_NOT_BE_BLANK.equals(violation.getMessage()));
    }

    @Test
    void whenFontAwesomeIconIsNull_ShouldHaveViolation() {
        // Arrange
        language.setFontAwesomeIcon(null);

        // Act
        final Set<ConstraintViolation<Language>> violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> FONT_AWESOME_ICON_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> MUST_NOT_BE_BLANK.equals(violation.getMessage()));
    }

    @Test
    void whenMdiIconIsNull_ShouldHaveViolation() {
        // Arrange
        language.setMdiIcon(null);

        // Act
        final Set<ConstraintViolation<Language>> violations = validator.validate(language);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> MDI_ICON_FIELD.equals(violation.getPropertyPath().toString()))
                .allMatch(violation -> MUST_NOT_BE_BLANK.equals(violation.getMessage()));
    }
}
