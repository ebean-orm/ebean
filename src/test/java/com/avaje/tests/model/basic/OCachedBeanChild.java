package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.annotation.CacheStrategy;

/**
 * Cached bean for testing caching implementation, especially relations.
 */
@CacheStrategy
@Entity
@Table(name = "o_cached_bean_child")
public class OCachedBeanChild {

    @Id
    Long id;

    @ManyToOne
    OCachedBean cachedBean;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OCachedBean getCachedBean() {
        return cachedBean;
    }

    public void setCachedBean(OCachedBean cachedBean) {
        this.cachedBean = cachedBean;
    }
}
