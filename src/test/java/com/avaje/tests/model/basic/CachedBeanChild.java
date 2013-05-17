package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.CacheStrategy;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Cached bean for testing caching implementation, especially relations.
 */
@CacheStrategy
@Entity
@Table(name = "o_cached_bean_child")
public class CachedBeanChild {

    @Id
    Long id;

    @ManyToOne
    CachedBean cachedBean;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CachedBean getCachedBean() {
        return cachedBean;
    }

    public void setCachedBean(CachedBean cachedBean) {
        this.cachedBean = cachedBean;
    }
}
