package br.com.munif.framework.vicente.security.domain;

import br.com.munif.framework.vicente.core.VicTenancyPolicy;
import br.com.munif.framework.vicente.core.VicTenancyType;
import br.com.munif.framework.vicente.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Set;

/**
 * @author munif
 */
@Entity
@Audited
@VicTenancyPolicy(VicTenancyType.COMMUM)
@Table(name = "vic_user")
public class User extends BaseEntity {

    @Column(name = "login", unique = true)
    private String login;
    @Column(name = "password")
    @JsonIgnore
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Group> groups;
    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    public User() {
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String stringGrupos() {
        if (this.getGroups() == null) {
            return null;
        }
        String s = "";
        for (Group g : this.getGroups()) {
            s += g.getCode() + ",";
        }

        return s;

    }

    public String stringOrganizacao() {
        if (organization == null) {
            return null;
        }
        return getOrganization().getId();
    }

}
