package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.CacheStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cached bean for testing caching implementation.
 */
@CacheStrategy
@Entity
@Table(name = "o_cached_bean")
public class CachedBean {

    @Id
    Long id;

    @ManyToMany
    List<Country> countries = new ArrayList<Country>();

    @OneToMany(mappedBy = "cachedBean", cascade = CascadeType.ALL)
    List<CachedBeanChild> children = new ArrayList<CachedBeanChild>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public List<CachedBeanChild> getChildren() {
        return children;
    }

    public void setChildren(List<CachedBeanChild> children) {
        this.children = children;
    }
}
