package de.muenchen.refarch.language;

import de.muenchen.refarch.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "languages_i18n")
@Getter
@Setter
public class Language extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String abbreviation;

    @NotBlank
    @Column(name = "font_awesome_icon", nullable = false)
    private String fontAwesomeIcon;

    @NotBlank
    @Column(name = "mdi_icon", nullable = false)
    private String mdiIcon;
}
