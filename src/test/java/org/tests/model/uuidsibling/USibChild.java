package org.tests.model.uuidsibling;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class USibChild extends Model {

    private static final long serialVersionUID = 738194912181571389L;

    @Id
    private UUID id;

    @SoftDelete
    private boolean deleted;

    @OneToOne(mappedBy = "child", cascade = CascadeType.REMOVE)
    private USibChildSibling childSibling;

    @ManyToOne
    private USibParent parent;

    public static Finder<UUID, USibChild> find = new Finder<>(USibChild.class);

    public USibChild() {}

    public USibChild(USibParent parent) {
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public USibChildSibling getChildSibling() {
        return childSibling;
    }

    public USibChild setChildSibling(USibChildSibling childSibling) {
        this.childSibling = childSibling;
        return this;
    }

    public USibParent getParent() {
        return parent;
    }

    public void setParent(USibParent parent) {
        this.parent = parent;
    }
}
