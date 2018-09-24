package org.tests.model.uuidsibling;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class USibParent extends Model {

    private static final long serialVersionUID = 4116545858939406149L;

    @Id
    private Long id;

    @SoftDelete
    private boolean deleted;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
    private List<USibChild> children;

    public static Finder<Long, USibParent> find = new Finder<>(USibParent.class);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<USibChild> getChildren() {
        return children;
    }

    public USibParent setChildren(List<USibChild> children) {
        this.children = children;
        return this;
    }
}
