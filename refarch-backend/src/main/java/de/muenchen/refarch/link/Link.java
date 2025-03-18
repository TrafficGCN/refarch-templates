package de.muenchen.refarch.link;

import de.muenchen.refarch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * Entity representing a link in the system.
 * Links can be either internal or external and can be associated with various content types.
 */
@Entity
@Table(name = "links")
@Getter
@Setter
@SuppressWarnings("PMD.ShortClassName")
public class Link extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Column(nullable = false)
    private String url;

    private String name;

    @Column(name = "font_awesome_icon")
    private String fontAwesomeIcon;

    @Column(name = "mdi_icon")
    private String mdiIcon;

    private String type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LinkScope scope;

    public String getLink() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
