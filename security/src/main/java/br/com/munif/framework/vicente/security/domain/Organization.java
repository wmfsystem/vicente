package br.com.munif.framework.vicente.security.domain;

import br.com.munif.framework.vicente.domain.BaseEntity;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * @author munif
 */
@Entity
@Audited
@Table(name = "vic_org", indexes = {
        @Index(name = "idx_vic_org_oi", columnList = "oi"),
        @Index(name = "idx_vic_org_ui", columnList = "ui"),
        @Index(name = "idx_vic_org_gi", columnList = "gi"),
        @Index(name = "idx_vic_org_rights", columnList = "rights")
})
public class Organization extends BaseEntity {

    @Column(name = "name")
    private String name;
    @Column(name = "code")
    private String code;

    @ManyToOne
    private Organization upper;

    public Organization() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Organization getUpper() {
        return upper;
    }

    public void setUpper(Organization upper) {
        this.upper = upper;
    }

}
