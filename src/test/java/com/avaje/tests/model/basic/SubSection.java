package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.annotation.CacheStrategy;

@CacheStrategy(useBeanCache=true)
@Entity
public class SubSection extends BasicDomain {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private Section section;

    private String title;

    public SubSection() {
    }

    public SubSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
