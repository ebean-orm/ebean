package org.tests.model.uuidsibling;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class USibChildSibling extends Model {

    private static final long serialVersionUID = 738194912181571389L;

    @Id
    private Long id;

    @SoftDelete
    private boolean deleted;

    @OneToOne
    private USibChild child;

    public static Finder<Long, USibChildSibling> find = new Finder<>(USibChildSibling.class);

    public USibChildSibling() {}

    public USibChildSibling(USibChild child) {
        this.child = child;
    }

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

    public USibChild getChild() {
        return child;
    }

    public USibChildSibling setChild(USibChild child) {
        this.child = child;
        return this;
    }
}
