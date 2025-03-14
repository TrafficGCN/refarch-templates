package de.muenchen.refarch.link;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkTest {

    private Validator validator;
    private Link link;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        link = new Link();
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.external);
        link.setFontAwesomeIcon("fa-link");
        link.setMdiIcon("mdi-link");
        link.setType("navigation");
    }

    @Test
    void whenAllFieldsAreValid_ShouldHaveNoViolations() {
        // Act
        var violations = validator.validate(link);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void whenUrlIsNull_ShouldHaveViolation() {
        // Arrange
        link.setUrl(null);

        // Act
        var violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("url"))
                .allMatch(violation -> violation.getMessage().contains("must not be blank"));
    }

    @Test
    void whenUrlIsBlank_ShouldHaveViolation() {
        // Arrange
        link.setUrl("");

        // Act
        var violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("url"))
                .allMatch(violation -> violation.getMessage().contains("must not be blank"));
    }

    @Test
    void whenScopeIsNull_ShouldHaveViolation() {
        // Arrange
        link.setScope(null);

        // Act
        var violations = validator.validate(link);

        // Assert
        assertThat(violations)
                .hasSize(1)
                .allMatch(violation -> violation.getPropertyPath().toString().equals("scope"))
                .allMatch(violation -> violation.getMessage().contains("must not be null"));
    }

    @Test
    void whenOptionalFieldsAreNull_ShouldHaveNoViolations() {
        // Arrange
        link.setName(null);
        link.setFontAwesomeIcon(null);
        link.setMdiIcon(null);
        link.setType(null);

        // Act
        var violations = validator.validate(link);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void getLink_ShouldReturnUrl() {
        // Arrange
        String url = "https://test.com";
        link.setUrl(url);

        // Act
        String result = link.getLink();

        // Assert
        assertThat(result).isEqualTo(url);
    }

    @Test
    void setUrl_ShouldSetUrl() {
        // Arrange
        String url = "https://test.com";

        // Act
        link.setUrl(url);

        // Assert
        assertThat(link.getUrl()).isEqualTo(url);
        assertThat(link.getLink()).isEqualTo(url);
    }
}
